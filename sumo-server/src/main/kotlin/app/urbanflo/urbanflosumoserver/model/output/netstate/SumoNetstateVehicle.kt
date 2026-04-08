package app.urbanflo.urbanflosumoserver.model.output.netstate

import app.urbanflo.urbanflosumoserver.model.SumoEntityId

/**
 * @see [SumoNetstateXml]
 */
data class SumoNetstateVehicle(
    val id: SumoEntityId,
    val pos: Double,
    val speed: Double
)