package app.urbanflo.urbanflosumoserver.model

data class SimulationAnalytics(
    val averageDuration: Double,
    val averageWaiting: Double,
    val averageTimeLoss: Double,
    val totalNumberOfCarsThatCompleted: Int,
    val simulationLength: Double
)
