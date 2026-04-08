package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.model.SimulationAnalytics
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.network.*
import app.urbanflo.urbanflosumoserver.model.output.netstate.SumoNetstateXml
import app.urbanflo.urbanflosumoserver.model.output.SumoSimulationOutput
import app.urbanflo.urbanflosumoserver.model.output.statistics.SumoStatisticsXml
import app.urbanflo.urbanflosumoserver.model.output.summary.SumoSummaryXml
import app.urbanflo.urbanflosumoserver.model.output.tripinfo.SumoTripInfoXml
import app.urbanflo.urbanflosumoserver.model.sumocfg.SumoCfg
import app.urbanflo.urbanflosumoserver.netconvert.NetconvertException
import app.urbanflo.urbanflosumoserver.netconvert.runNetconvert
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

private val logger = KotlinLogging.logger {}

/**
 * Storage service, using the file system as storage.
 */
@Service
class FilesystemStorageService @Autowired constructor(properties: StorageProperties) : StorageService {
    /**
     * Root directory for all uploads
     */
    private lateinit var uploadsDir: Path

    /**
     * Jackson XMl mapper
     */
    private val xmlMapper = XmlMapper()

    /**
     * Jackson JSON mapper
     */
    private val jsonMapper = jacksonObjectMapper()

    init {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        xmlMapper.registerModule(kotlinModule())
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        jsonMapper.registerModules(JavaTimeModule())
        this.uploadsDir = Paths.get(properties.location)
        try {
            Files.createDirectories(uploadsDir)
        } catch (e: IOException) {
            throw IllegalStateException("Cannot create uploads directory", e)
        }
    }

    override fun store(network: SumoNetwork): SimulationInfo {
        var id: SimulationId
        var simulationDir: Path
        do {
            id = UUID.randomUUID().toString()
            simulationDir = uploadsDir.resolve(Paths.get(id).normalize()).toAbsolutePath()
        } while (simulationDir.exists())
        simulationDir.createDirectory()

        val now = currentTime()
        val info = SimulationInfo(id, network.documentName, now, now)
        try {
            writeFiles(info, network, simulationDir)
        } catch (e: Exception) {
            delete(id) // perform cleanup
            throw e
        }

        return info
    }

    override fun store(simulationId: SimulationId, network: SumoNetwork): SimulationInfo {
        val now = currentTime()
        val oldInfo = this.info(simulationId)
        val oldNetwork = this.export(simulationId)
        assert(oldInfo.id == simulationId)
        val simulationDir = getSimulationDir(simulationId)
        val newInfo = SimulationInfo(oldInfo.id, network.documentName, oldInfo.createdAt, now)
        try {
            writeFiles(newInfo, network, simulationDir)
        } catch (e: Exception) {
            // restore old network data
            var retryCount = 10
            while (retryCount > 0) {
                try {
                    writeFiles(oldInfo, oldNetwork, simulationDir)
                    break
                } catch (re: Exception) {
                    retryCount--
                    if (retryCount == 0) {
                        logger.error(re) { "Cannot restore simulation after invalid modification" }
                    }
                }
            }
            throw e
        }
        return newInfo
    }

    /**
     * Common function for writing XML files and converting network using `netconvert`
     */
    private fun writeFiles(simulationInfo: SimulationInfo, network: SumoNetwork, simulationDir: Path) {
        if (!simulationDir.exists()) {
            throw IllegalStateException("simulationDir does not exist")
        }
        val simulationId = simulationInfo.id

        // save network as XML
        val nod = network.nodesXml()
        val nodPath = SumoNodesXml.filePath(simulationId, simulationDir)
        val edg = network.edgesXml()
        val edgPath = SumoEdgesXml.filePath(simulationId, simulationDir)
        val con = network.connectionsXml()
        val conPath = SumoConnectionsXml.filePath(simulationId, simulationDir)
        val rou = network.routesXml()
        val rouPath = SumoRoutesXml.filePath(simulationId, simulationDir)
        try {
            nodPath.toFile().writeText(xmlMapper.writeValueAsString(nod))
            edgPath.toFile().writeText(xmlMapper.writeValueAsString(edg))
            conPath.toFile().writeText(xmlMapper.writeValueAsString(con))
            rouPath.toFile().writeText(xmlMapper.writeValueAsString(rou))
        } catch (e: JsonProcessingException) {
            logger.error(e) { "Invalid network data" }
            throw StorageBadRequestException("Invalid network data", e)
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            throw StorageException("Cannot save files", e)
        }

        // run netconvert
        try {
            val netPath = runNetconvert(simulationId, simulationDir, nodPath, edgPath, conPath)
            assert(netPath.exists())
            // create sumocfg
            val sumocfg = SumoCfg(simulationId, netPath, rouPath)
            val sumocfgPath = SumoCfg.filePath(simulationId, simulationDir)
            sumocfgPath.toFile().writeText(xmlMapper.writeValueAsString(sumocfg))
        } catch (e: NetconvertException) {
            logger.error(e) { "Cannot convert XML files" }
            throw StorageException("Cannot convert XML files", e)
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            throw StorageException("Cannot save files", e)
        }

        // save info file
        val infoPath = SimulationInfo.filePath(simulationDir)
        try {
            infoPath.toFile().writeText(jsonMapper.writeValueAsString(simulationInfo))
        } catch (e: IOException) {
            logger.error(e) { "Cannot save files" }
            throw StorageException("Cannot save files", e)
        }
    }

    override fun load(id: SimulationId, label: String): SimulationInstance {
        val simulationDir = uploadsDir.resolve(Paths.get(id).normalize())
        val cfgPath = simulationDir.resolve("$id.sumocfg").normalize().toAbsolutePath()
        if (simulationDir.exists()) {
            return SimulationInstance(id, label, cfgPath)
        } else {
            throw StorageSimulationNotFoundException(id)
        }
    }

    override fun delete(id: SimulationId) {
        val simulationDir = getSimulationDir(id).toFile()
        simulationDir.listFiles()?.forEach { file -> file.delete() }
        if (!simulationDir.delete()) {
            throw StorageSimulationNotFoundException(id)
        }
    }

    override fun info(id: SimulationId): SimulationInfo {
        val simulationDir = getSimulationDir(id)
        val infoFile = SimulationInfo.filePath(simulationDir).toFile()
        if (!(simulationDir.exists() && infoFile.exists())) {
            throw StorageSimulationNotFoundException(id)
        }
        return jsonMapper.readValue(infoFile)
    }

    override fun export(simulationId: SimulationId): SumoNetwork {
        val simulationDir = getSimulationDir(simulationId)
        if (simulationDir.exists()) {
            try {
                val nodPath = SumoNodesXml.filePath(simulationId, simulationDir)
                val edgPath = SumoEdgesXml.filePath(simulationId, simulationDir)
                val conPath = SumoConnectionsXml.filePath(simulationId, simulationDir)
                val rouPath = SumoRoutesXml.filePath(simulationId, simulationDir)
                val infoPath = SimulationInfo.filePath(simulationDir)
                val nod: SumoNodesXml = xmlMapper.readValue(nodPath.toFile())
                val edg: SumoEdgesXml = xmlMapper.readValue(edgPath.toFile())
                val con: SumoConnectionsXml = xmlMapper.readValue(conPath.toFile())
                val rou: SumoRoutesXml = xmlMapper.readValue(rouPath.toFile())
                val info: SimulationInfo = jsonMapper.readValue(infoPath.toFile())
                return SumoNetwork(info, nod, edg, con, rou)
            } catch (e: IOException) {
                logger.error(e) { "Cannot export network" }
                throw StorageException("Cannot export network", e)
            }
        } else {
            throw StorageSimulationNotFoundException(simulationId)
        }
    }

    override fun listAll(): List<SimulationInfo> {
        val simulationDirs = uploadsDir.listDirectoryEntries()
        return simulationDirs.map<Path, SimulationInfo> { simulation ->
            val infoFile = SimulationInfo.filePath(simulation).toFile()
            jsonMapper.readValue(infoFile)
        }.sortedByDescending { it.lastModifiedAt }
    }

    @Deprecated("Please use the individual getOutput() functions")
    override fun getSimulationOutput(simulationId: SimulationId): SumoSimulationOutput {
        val tripInfoPath = SumoTripInfoXml.filePath(simulationId, getSimulationDir(simulationId))
        val netstatePath = SumoNetstateXml.filePath(simulationId, getSimulationDir(simulationId))

        val tripInfo: SumoTripInfoXml = getOutputFile(simulationId, tripInfoPath)
        val netstate: SumoNetstateXml = getOutputFile(simulationId, netstatePath)

        return SumoSimulationOutput(tripInfo.tripInfos, netstate.timesteps)
    }

    override fun getTripInfoOutput(simulationId: SimulationId): SumoTripInfoXml {
        val tripInfoPath = SumoTripInfoXml.filePath(simulationId, getSimulationDir(simulationId))
        return getOutputFile(simulationId, tripInfoPath)
    }

    override fun getNetStateOutput(simulationId: SimulationId): SumoNetstateXml {
        val netstatePath = SumoNetstateXml.filePath(simulationId, getSimulationDir(simulationId))
        return getOutputFile(simulationId, netstatePath)
    }

    override fun getSummaryOutput(simulationId: SimulationId): SumoSummaryXml {
        val summaryPath = SumoSummaryXml.filePath(simulationId, getSimulationDir(simulationId))
        return getOutputFile(simulationId, summaryPath)
    }

    override fun getStatisticsOutput(simulationId: SimulationId): SumoStatisticsXml {
        val statisticsPath = SumoStatisticsXml.filePath(simulationId, getSimulationDir(simulationId))
        return getOutputFile(simulationId, statisticsPath)
    }

    override fun deleteSimulationOutput(simulationId: SimulationId) {
        SumoTripInfoXml.filePath(simulationId, getSimulationDir(simulationId)).toFile().delete()
        SumoNetstateXml.filePath(simulationId, getSimulationDir(simulationId)).toFile().delete()
        SumoStatisticsXml.filePath(simulationId, getSimulationDir(simulationId)).toFile().delete()
    }

    @Deprecated("Please use getStatisticsOutput() as it's faster and gives more information")
    override fun getSimulationAnalytics(simulationId: SimulationId): SimulationAnalytics {
        val tripInfo = getTripInfoOutput(simulationId).tripInfos
        val netState = getNetStateOutput(simulationId).timesteps

        // Average duration: The average time each vehicle needed to accomplish the route in simulation seconds
        val averageDuration = tripInfo.map { it.duration }.average()

        // Waiting time: The average time in which vehicles speed was below or equal 0.1 m/s in simulation seconds
        val averageWaiting = tripInfo.map { it.waitingTime }.average()

        // Time loss: The time lost due to driving below the ideal speed. (ideal speed includes the individual speedFactor; slowdowns due to intersections etc. will incur timeLoss, scheduled stops do not count) in simulation seconds
        val averageTimeLoss = tripInfo.map { it.timeLoss }.average()

        // Total number of cars that reached their destination. Can work this out with vaporised variable
        val totalNumberOfCarsThatCompleted = tripInfo.count() - tripInfo.count { !it.vaporized.isNullOrEmpty() }

        val simulationLength = netState.lastOrNull()?.time ?: 0.0

        return SimulationAnalytics(
            averageDuration,
            averageWaiting,
            averageTimeLoss,
            totalNumberOfCarsThatCompleted,
            simulationLength,
        )
    }

    /**
     * Returns current time as [OffsetDateTime]
     */
    private fun currentTime() = OffsetDateTime.now(ZoneOffset.UTC)

    /**
     * Common function for reading and parsing output XML files
     */
    private inline fun <reified T>getOutputFile(simulationId: SimulationId, path: Path): T {
        if (!path.exists()) {
            throw StorageSimulationNotFoundException(simulationId, "Simulation hasn't started")
        }

        var retryCount = 0
        while (true) {
            try {
                return xmlMapper.readValue(path.toFile())
            } catch (e: IOException) {
                if (retryCount < 3) {
                    // Add arbitrary delay to give libtraci time to close the output files
                    Thread.sleep(1000)
                    retryCount++
                } else {
                    logger.error(e) { "Cannot read simulation output. Either simulation hasn't started or simulation wasn't closed properly" }
                    throw StorageSimulationNotFoundException(simulationId, "Either simulation hasn't started or simulation wasn't closed properly", e)
                }
            }
        }
    }

    /**
     * Get path to simulation directory for the given ID
     */
    private fun getSimulationDir(simulationId: SimulationId) = if (simulationId.isNotEmpty()) { // if simulationId is empty, it returns uploads dir which could be disastrous
        uploadsDir.resolve(Paths.get(simulationId).normalize())
    } else {
        throw StorageBadRequestException("Simulation ID must not be empty")
    }
}