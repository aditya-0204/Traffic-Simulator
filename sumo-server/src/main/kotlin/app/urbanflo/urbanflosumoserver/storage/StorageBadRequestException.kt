package app.urbanflo.urbanflosumoserver.storage

/**
 * Exception for invalid storage requests.
 */
class StorageBadRequestException : StorageException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}