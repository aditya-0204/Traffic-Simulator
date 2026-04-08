package app.urbanflo.urbanflosumoserver.model


enum class SimulationMessageType {
    START, STOP
}

/**
 * WebSocket simulation message to signal the simulation instance to start or stop.
 */
data class SimulationMessageRequest(var status: SimulationMessageType)