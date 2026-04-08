package app.urbanflo.urbanflosumoserver.simulation

import app.urbanflo.urbanflosumoserver.model.SimulationException
import app.urbanflo.urbanflosumoserver.model.VehicleData
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.constraints.NotEmpty
import org.eclipse.sumo.libtraci.Simulation
import org.eclipse.sumo.libtraci.StringVector
import org.eclipse.sumo.libtraci.Vehicle
import reactor.core.publisher.Flux
import java.net.ServerSocket
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock


typealias SimulationStep = Map<String, VehicleData>
typealias SimulationId = @NotEmpty String

// taken from traci python code
private const val DEFAULT_NUM_RETRIES = 60
private val logger = KotlinLogging.logger {}

/**
 * Class for running simulation instances.
 */
class SimulationInstance(
    /**
     * Simulation ID
     */
    val simulationId: SimulationId,
    /**
     * TraCI label for this simulation. Can be anything unique but is currently the WebSocket session ID
     */
    val label: String,
    cfgPath: Path
) : Iterator<SimulationStep> {
    /**
     * Map of vehicle colours by vehicle ID
     */
    private val vehicleColors: MutableMap<String, String> = mutableMapOf()
    /**
     * TraCI port for this simulation's connection
     */
    private val port: Int = getNextAvailablePort()

    /**
     * Time frame for each simulation step. Note that this is only a target and it can vary depending on performance and
     * other factors.
     */
    private var frameTime = setSimulationSpeed(1)

    /**
     * [Flux] instance for simulation steps
     */
    var flux = Flux.create<SimulationStep> { sink ->
        while (hasNext()) {
            sink.next(next())
        }
        sink.complete()
    }

    /**
     * If `true`, the simulation should be closed on next invocation of [hasNext] if it hasn't already closed.
     */
    @Volatile
    private var shouldStop = false

    /**
     * If `true`, the simulation has been closed
     */
    @Volatile
    private var connectionClosed = false

    /**
     * Calculate the frame time (in ms) for the given simulation speed.
     *
     * The framerate at 1x speed is 60fps, so its frame time would be around 16.7ms.
     */
    fun setSimulationSpeed(speed: Long): Duration = Duration.ofMillis(1000 / (60 * speed))

    init {
        try {
            logger.info { "Connecting to SUMO with port $port and label $label" }
            lock.lock()
            logger.trace { "$label: lock acquired" }
            Simulation.start(
                StringVector(arrayOf("sumo", "-c", cfgPath.toString())),
                port,
                DEFAULT_NUM_RETRIES,
                label
            )

        } finally {
            logger.trace { "$label: releasing lock" }
            lock.unlock()
        }
    }

    override fun hasNext(): Boolean {
        try {
            lock.lock()
            logger.trace { "$label: lock acquired" }
            if (connectionClosed) {
                return false
            }
            Simulation.switchConnection(label)
            if (shouldStop) {
                closeSimulation()
                return false
            }

            val expected = Simulation.getMinExpectedNumber() > 0
            if (!expected) {
                closeSimulation()
            }

            return expected
        } catch (e: Exception) {
            logger.error(e) { "Error in advancing simulation step" }
            throw SimulationException("Error in advancing simulation step: ${e.message}")
        } finally {
            logger.trace { "$label: releasing lock" }
            lock.unlock()
        }
    }

    override fun next(): SimulationStep {
        val start = Instant.now()
        val pairs: Map<String, VehicleData>
        try {
            lock.lock()
            logger.trace { "$label: lock acquired" }

            Simulation.switchConnection(label)
            Simulation.step()

            pairs = Vehicle.getIDList().associateWith { vehicleId ->
                val rawPosition = Vehicle.getPosition(vehicleId)
                val position = Simulation.convertGeo(rawPosition.x, rawPosition.y, false)
                val acceleration = Vehicle.getAcceleration(vehicleId)
                val speed = Vehicle.getSpeed(vehicleId)
                val color = getVehicleColor(vehicleId)
                val laneIndex = Vehicle.getLaneIndex(vehicleId)
                val laneId = Vehicle.getLaneID(vehicleId)
                VehicleData(
                    vehicleId,
                    Pair(position.x, position.y),
                    color,
                    acceleration,
                    speed,
                    Pair(laneIndex, laneId)
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Error in advancing simulation step" }
            throw SimulationException("Error in advancing simulation step: ${e.message}")
        } finally {
            logger.trace { "$label: releasing lock" }
            lock.unlock()
        }
        // we've left the critical section here, so the sleep can be done asynchronously
        val end = Instant.now()
        val delay = frameTime.toMillis() - Duration.between(start, end).toMillis()
        if (delay > 0) {
            logger.trace { "$label: sleeping for $delay ms" }
            Thread.sleep(delay)
        }
        return pairs
    }

    /**
     * Returns the vehicle colour (in HTML hex format) for the vehicle ID, or generates one if it's not yet assigned.
     */
    private fun getVehicleColor(vehicleId: String): String {
        // if let color = vehicleColors[vehicleId] { return color } else { assign random colour to vehicle and return }
        val color = vehicleColors[vehicleId] ?: run {
            val newColor = "#ffff00"
            vehicleColors[vehicleId] = newColor
            newColor
        }
        return color
    }

    /**
     * Signal the instance that simulation should be stopped.
     *
     * Note that this doesn't actually close the connection to TraCI, as this is naturally done on next invocation of
     * [hasNext] by the WebSocket stream.
     */
    fun stopSimulation() {
        shouldStop = true
    }

    /**
     * Forcibly mark simulation to be stopped and close the connection to TraCI.
     *
     * Note that this should be only called for server shutdown. Bad things will happen when called in any other
     * situations.
     */
    fun forceCloseConnectionOnServerShutdown() {
        try {
            lock.lock()
            logger.trace { "$label: lock acquired" }
            Simulation.switchConnection(label)
            stopSimulation()
            closeSimulation()
        } finally {
            logger.trace { "$label: releasing lock" }
            lock.unlock()
        }
    }

    /**
     * Close the connection to TraCI.
     *
     * Note: there's no lock here as this method is assumed to only be called inside a locked section
     */
    private fun closeSimulation() {
        logger.info { "Closing connection with ID $simulationId and label: ${Simulation.getLabel()}" }
        Simulation.close()
        connectionClosed = true
    }

    companion object {
        /**
         * Thread lock to prevent race conditions
         */
        private val lock = ReentrantLock()

        /**
         * returns the next random port that is available for TraCI connection.
         *
         * [Source/adapted from](https://stackoverflow.com/a/2675416)
         */
        private fun getNextAvailablePort(): Int {
            val socket = ServerSocket(0)
            val socketPort = socket.localPort
            socket.close()
            return socketPort
        }
    }
}