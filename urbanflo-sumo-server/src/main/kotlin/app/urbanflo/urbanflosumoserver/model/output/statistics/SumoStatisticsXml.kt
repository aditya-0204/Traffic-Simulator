package app.urbanflo.urbanflosumoserver.model.output.statistics

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

/**
 * Data class for SUMO [statistic output.](https://sumo.dlr.de/docs/Simulation/Output/StatisticOutput.html)
 */
@JacksonXmlRootElement(localName = "statistics")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SumoStatisticsXml(
    val performance: SumoPerformanceStatistics,
    val vehicles: SumoVehiclesStatistics,
    val teleports: SumoTeleportsStatistics,
    val safety: SumoSafetyStatistics,
    val persons: SumoPersonsStatistics,
    val personTeleports: SumoPersonTeleports,
    val vehicleTripStatistics: SumoVehicleTripStatistics,
    val pedestrianStatistics: SumoPedestrianStatistics,
    val rideStatistics: SumoRideStatistics,
    val transportStatistics: SumoTransportStatistics
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.statistics.xml"
    }
}