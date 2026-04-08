package app.urbanflo.urbanflosumoserver.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Component which stores the root directory for all simulation files.
 */
@Component
class StorageProperties {
    @Value("\${urbanflo.storage.location:uploads}")
    lateinit var location: String
}