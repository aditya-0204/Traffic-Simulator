package app.urbanflo.urbanflosumoserver.model.output

import app.urbanflo.urbanflosumoserver.model.output.netstate.SumoNetstateTimestep
import app.urbanflo.urbanflosumoserver.model.output.tripinfo.SumoTripInfo

data class SumoSimulationOutput(
    val tripInfo: List<SumoTripInfo>,
    val netstate: List<SumoNetstateTimestep>
)
