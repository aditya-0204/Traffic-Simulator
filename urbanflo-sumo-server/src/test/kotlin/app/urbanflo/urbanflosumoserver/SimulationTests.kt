package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.model.output.statistics.SumoStatisticsXml
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance
import app.urbanflo.urbanflosumoserver.storage.StorageService
import app.urbanflo.urbanflosumoserver.storage.StorageSimulationNotFoundException
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.annotation.Async
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Tests for simulation instances.
 */
private val logger = KotlinLogging.logger {}
private const val DOUBLE_CMP_TOLERANCE = 0.1

        @SpringBootTest
class SimulationTests(@Autowired private val storageService: StorageService) {
    @Value("\${urbanflo.storage.location:uploads}")
    lateinit var uploadsLocation: String



    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    val simpleNetwork: SumoNetwork = jsonMapper.readValue(ClassPathResource("simple-network.json").file)
    val fourWayIntersection: SumoNetwork = jsonMapper.readValue(ClassPathResource("4-way-intersection.json").file)
    val multiLaneNetwork: SumoNetwork = jsonMapper.readValue(ClassPathResource("multilane.json").file)

    init {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        xmlMapper.registerModule(kotlinModule())
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        jsonMapper.registerModules(JavaTimeModule())
    }

    @BeforeEach
    fun deleteAllUploads() {
        File(uploadsLocation).listFiles()?.forEach { simulation ->
            simulation.listFiles()?.forEach { file ->
                file.delete()
            }
            simulation.delete()
        }
    }

    @Test
    fun testSimpleNetworkSimulation() {
        val info = storageService.store(simpleNetwork)
        val simulation = storageService.load(info.id, generateSimulationLabel())
        // TODO: learn how to test the flux

        assertTrue(simulation.hasNext())
        val future = runSimulation(simulation)
        val statistics = future.get()
        logger.info { "Statistics: ${jsonMapper.writeValueAsString(statistics)}" }
        assertFalse(simulation.hasNext())

        // test analytics
        val analytics = storageService.getSimulationAnalytics(simulation.simulationId)
        assertEquals(statistics.vehicleTripStatistics.duration, analytics.averageDuration, DOUBLE_CMP_TOLERANCE)
        assertEquals(statistics.vehicleTripStatistics.waitingTime, analytics.averageWaiting, DOUBLE_CMP_TOLERANCE)
        assertEquals(statistics.vehicleTripStatistics.timeLoss, analytics.averageTimeLoss, DOUBLE_CMP_TOLERANCE)
        assertEquals(statistics.vehicleTripStatistics.count, analytics.totalNumberOfCarsThatCompleted)
    }

    @Test
    fun testConcurrentSimulations() {
        val simpleInfo = storageService.store(simpleNetwork)
        val fourWayInfo = storageService.store(fourWayIntersection)
        val simpleSimulation = storageService.load(simpleInfo.id, generateSimulationLabel())
        val fourWaySimulation = storageService.load(fourWayInfo.id, generateSimulationLabel())

        val simpleFuture = runSimulation(simpleSimulation)
        val fourWayFuture = runSimulation(fourWaySimulation)
        val simpleStatistics = simpleFuture.get()
        val fourWayStatistics = fourWayFuture.get()
        logger.info { "Statistics: ${jsonMapper.writeValueAsString(simpleStatistics)}\n${jsonMapper.writeValueAsString(fourWayStatistics)}" }

        assertFalse(simpleSimulation.hasNext())
        assertFalse(fourWaySimulation.hasNext())
    }

    @Test
    fun testNoSimulationOutput() {
        val info = storageService.store(simpleNetwork)
        assertThrows<StorageSimulationNotFoundException> {
            storageService.getSimulationAnalytics(info.id)
        }
    }

    @Test
    fun testMultiLaneNetwork() {
        val info = storageService.store(multiLaneNetwork)
        val simulation = storageService.load(info.id, generateSimulationLabel())

        assertTrue(simulation.hasNext())
        val future = runSimulation(simulation)
        val statistics = future.get()
        logger.info { "Statistics: ${jsonMapper.writeValueAsString(statistics)}" }
        assertFalse(simulation.hasNext())

        // test analytics
        val analytics = storageService.getSimulationAnalytics(simulation.simulationId)
        assertEquals(statistics.vehicleTripStatistics.duration, analytics.averageDuration, DOUBLE_CMP_TOLERANCE)
        assertEquals(statistics.vehicleTripStatistics.waitingTime, analytics.averageWaiting, DOUBLE_CMP_TOLERANCE)
        assertEquals(statistics.vehicleTripStatistics.timeLoss, analytics.averageTimeLoss, DOUBLE_CMP_TOLERANCE)
        assertEquals(statistics.vehicleTripStatistics.count, analytics.totalNumberOfCarsThatCompleted)
    }

    @Async
    fun runSimulation(simulation: SimulationInstance): Future<SumoStatisticsXml> {
        var i = 0
        while (true) {
            if (!simulation.hasNext()) { // use the side effect of hasNext() to actually close the connection
                break
            } else if (i < 100) {
                simulation.next()
            } else {
                simulation.stopSimulation()
            }
            i++
        }
        val statistics = storageService.getStatisticsOutput(simulation.simulationId)
        return CompletableFuture.completedFuture(statistics)
    }

    private fun generateSimulationLabel() = UUID.randomUUID().toString()

    companion object {
        @JvmStatic
        @BeforeAll
        fun loadTraCI() {
            System.loadLibrary("libtracijni")
        }
    }
}