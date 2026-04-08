package app.urbanflo.urbanflosumoserver.storage

/**
 * Exception for any storage errors. Some errors are covered by subclasses of this exception, such as
 * [StorageBadRequestException].
 */
open class StorageException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}