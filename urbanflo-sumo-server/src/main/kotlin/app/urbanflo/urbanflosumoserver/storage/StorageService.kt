package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.model.SimulationAnalytics
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.model.output.netstate.SumoNetstateXml
import app.urbanflo.urbanflosumoserver.model.output.SumoSimulationOutput
import app.urbanflo.urbanflosumoserver.model.output.statistics.SumoStatisticsXml
import app.urbanflo.urbanflosumoserver.model.output.summary.SumoSummaryXml
import app.urbanflo.urbanflosumoserver.model.output.tripinfo.SumoTripInfoXml
import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import app.urbanflo.urbanflosumoserver.simulation.SimulationInstance

/**
 * Interface for storage service classes.
 */
interface StorageService {
    /**
     * Store a new network.
     *
     * @return New simulation info
     * @throws StorageBadRequestException if network data is invalid
     * @throws StorageException if simulation cannot be saved
     */
    fun store(network: SumoNetwork): SimulationInfo

    /**
     * Modify an existing network for the given simulation ID.
     *
     * @return Updated simulation info
     * @throws StorageBadRequestException if network data is invalid
     * @throws StorageException if simulation cannot be saved
     */
    fun store(simulationId: SimulationId, network: SumoNetwork): SimulationInfo

    /**
     * Load a simulation to be run.
     *
     * @param id Simulation ID
     * @param label Any string used to uniquely identify the simulation instance to TraCI
     * @throws StorageSimulationNotFoundException if simulation cannot be found
     */
    fun load(id: SimulationId, label: String): SimulationInstance

    /**
     * Delete a simulation by simulation ID.
     * @throws StorageSimulationNotFoundException if simulation cannot be found
     */
    fun delete(id: SimulationId)

    /**
     * Get information about a simulation for the given ID.
     * @throws StorageSimulationNotFoundException if simulation cannot be found
     */
    fun info(id: SimulationId): SimulationInfo

    /**
     * Export network data of a simulation for the given ID.
     * @throws StorageSimulationNotFoundException if simulation cannot be found
     */
    fun export(simulationId: SimulationId): SumoNetwork

    /**
     * List all simulation information.
     */
    fun listAll(): List<SimulationInfo>

    @Deprecated("Please use the individual getOutput() functions")
    fun getSimulationOutput(simulationId: SimulationId): SumoSimulationOutput

    /**
     * returns a SUMO [`tripinfo` output](https://sumo.dlr.de/docs/Simulation/Output/TripInfo.html) for the given ID.
     *
     * @throws StorageSimulationNotFoundException if simulation cannot be found, simulation hasn't started or simulation wasn't closed properly
     */
    fun getTripInfoOutput(simulationId: SimulationId): SumoTripInfoXml

    /**
     * returns a SUMO [`netstate` output](https://sumo.dlr.de/docs/Simulation/Output/RawDump.html) output for the given ID.
     *
     * @throws StorageSimulationNotFoundException if simulation cannot be found, simulation hasn't started or simulation wasn't closed properly
     */
    fun getNetStateOutput(simulationId: SimulationId): SumoNetstateXml

    /**
     * returns a SUMO [summary output](https://sumo.dlr.de/docs/Simulation/Output/Summary.html) for the given ID.
     *
     * @throws StorageSimulationNotFoundException if simulation cannot be found, simulation hasn't started or simulation wasn't closed properly
     */
    fun getSummaryOutput(simulationId: SimulationId): SumoSummaryXml

    /**
     * returns a SUMO [statistic output](https://sumo.dlr.de/docs/Simulation/Output/StatisticOutput.html) for the given ID.
     *
     * @throws StorageSimulationNotFoundException if simulation cannot be found, simulation hasn't started or simulation wasn't closed properly
     */
    fun getStatisticsOutput(simulationId: SimulationId): SumoStatisticsXml

    /**
     * Delete simulation output for the given ID.
     */
    fun deleteSimulationOutput(simulationId: SimulationId)

    @Deprecated("Please use getStatisticsOutput() as it's faster and gives more information")
    fun getSimulationAnalytics(simulationId: SimulationId): SimulationAnalytics
}