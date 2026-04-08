package app.urbanflo.urbanflosumoserver.model.output.summary

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

/**
 * Data class for SUMO [summary output.](https://sumo.dlr.de/docs/Simulation/Output/Summary.html)
 */
@JacksonXmlRootElement(localName = "summary")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SumoSummaryXml(
    @field:JacksonXmlProperty(localName = "step")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val timesteps: List<SumoSummaryStep> = listOf()
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.summary.xml"
    }
}
