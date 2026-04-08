package app.urbanflo.urbanflosumoserver.model.output.tripinfo

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

/**
 * Data class for SUMO [`tripinfo` output,](https://sumo.dlr.de/docs/Simulation/Output/TripInfo.html)
 */
@JacksonXmlRootElement(localName = "tripinfos")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SumoTripInfoXml(
    @field:JacksonXmlProperty(localName = "tripinfo")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val tripInfos: List<SumoTripInfo> = listOf()
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.tripinfo.xml"
    }
}
