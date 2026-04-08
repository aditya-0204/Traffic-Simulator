package app.urbanflo.urbanflosumoserver.model

/**
 * Vehicle data produced by TraCI from each simulation step.
 */
data class VehicleData(
    val vehicleId: String,
    val position: Pair<Double, Double>, // [x, y]
    val color: String,
    val acceleration: Double,
    val speed: Double,
    val lane: Pair<Int, String> // [index, id]
)