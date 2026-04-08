package app.urbanflo.urbanflosumoserver.model.sumocfg

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.nio.file.Path

data class SumoCfgNetstateOutput(
    @field:JacksonXmlProperty(isAttribute = true)
    val value: String
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.netstate.xml"
    }
}