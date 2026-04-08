package app.urbanflo.urbanflosumoserver.storage

import app.urbanflo.urbanflosumoserver.simulation.SimulationId

/**
 * Exception for when the simulation cannot be found.
 */
class StorageSimulationNotFoundException : StorageException {
    constructor(simulationId: SimulationId): super("No such simulation with ID $simulationId")
    constructor(simulationId: SimulationId, cause: Throwable): super("No such simulation with ID $simulationId", cause)
    constructor(simulationId: SimulationId, additionalMessage: String): super("No such simulation with ID $simulationId: $additionalMessage")
    constructor(simulationId: SimulationId, additionalMessage: String, cause: Throwable): super("No such simulation with ID $simulationId: $additionalMessage", cause)
}