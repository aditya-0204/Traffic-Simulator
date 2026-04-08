package app.urbanflo.urbanflosumoserver.model.sumocfg

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

/**
 * Data class for [SUMO configuration XML.](https://sumo.dlr.de/xsd/sumoConfiguration.xsd)
 */
@JacksonXmlRootElement(localName = "configuration")
class SumoCfg(
    simulationId: SimulationId,
    netPath: Path,
    rouPath: Path
) {
    val input: SumoCfgInput
    val output: SumoCfgOutput

    init {
        input = SumoCfgInput(
            SumoCfgNetFile(netPath.fileName.toString()),
            SumoCfgRouteFiles(rouPath.fileName.toString())
        )
        output = SumoCfgOutput(
            SumoCfgTripInfoOutput(SumoCfgTripInfoOutput.fileName(simulationId)),
            SumoCfgNetstateOutput(SumoCfgNetstateOutput.fileName(simulationId)),
            SumoCfgSummaryOutput(SumoCfgSummaryOutput.fileName(simulationId)),
            SumoCfgStatisticOutput(SumoCfgStatisticOutput.fileName(simulationId))
        )
    }

    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.sumocfg"
    }
}
