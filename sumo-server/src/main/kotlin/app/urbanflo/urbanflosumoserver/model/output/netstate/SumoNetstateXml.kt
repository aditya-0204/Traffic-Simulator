package app.urbanflo.urbanflosumoserver.model.output.netstate

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

/**
 * Data class for SUMO [`netstate` output.](https://sumo.dlr.de/docs/Simulation/Output/RawDump.html)
 */
@JacksonXmlRootElement(localName = "netstate")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SumoNetstateXml(
    @field:JacksonXmlProperty(localName = "timestep")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val timesteps: List<SumoNetstateTimestep> = listOf()
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.netstate.xml"
    }
}
