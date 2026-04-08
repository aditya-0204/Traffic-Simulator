package app.urbanflo.urbanflosumoserver.model

/**
 * Response body for all errors.
 */
data class ErrorResponse(
    /**
     * Error message
     */
    val error: String,
    /**
     * Mapping of fields in request body which failed validation or contains errors.
     */
    val errorFields: Map<String, String?>? = null
)