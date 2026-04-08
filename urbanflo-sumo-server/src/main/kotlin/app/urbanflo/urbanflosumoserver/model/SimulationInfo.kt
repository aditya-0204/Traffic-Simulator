package app.urbanflo.urbanflosumoserver.model

import java.nio.file.Path
import java.time.OffsetDateTime

/**
 * Data class for simulation information.
 */
data class SimulationInfo(
    /**
     * Simulation ID
     */
    val id: String,
    /**
     * Document name as sent by frontend.
     */
    val documentName: String,
    /**
     * Creation date, in ISO8601 format.
     */
    val createdAt: OffsetDateTime,
    /**
     * Last modified date, in ISO8601 format.
     */
    val lastModifiedAt: OffsetDateTime
) {
    companion object {
        fun filePath(simulationDir: Path): Path =
            simulationDir.resolve(fileName()).normalize().toAbsolutePath()

        fun fileName() = "info.json"
    }
}
