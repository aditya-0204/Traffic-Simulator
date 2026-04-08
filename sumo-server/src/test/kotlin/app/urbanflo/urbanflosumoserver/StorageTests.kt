package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import app.urbanflo.urbanflosumoserver.storage.FilesystemStorageService
import app.urbanflo.urbanflosumoserver.storage.StorageBadRequestException
import app.urbanflo.urbanflosumoserver.storage.StorageException
import app.urbanflo.urbanflosumoserver.storage.StorageSimulationNotFoundException
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import java.io.File

/**
 * Tests for storage service.
 */
@SpringBootTest
class StorageTests(@Autowired private val storageService: FilesystemStorageService) {
    @Value("\${urbanflo.storage.location:uploads}")
    lateinit var uploadsLocation: String

    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    val simpleNetwork: SumoNetwork = jsonMapper.readValue(ClassPathResource("simple-network.json").file)
    val fourWayIntersection: SumoNetwork = jsonMapper.readValue(ClassPathResource("4-way-intersection.json").file)
    val noEdgeNetwork: SumoNetwork = jsonMapper.readValue(ClassPathResource("no-edges.json").file)

    @BeforeEach
    fun deleteAllUploads() {
        File(uploadsLocation).listFiles()?.forEach { simulation ->
            simulation.listFiles()?.forEach { file ->
                file.delete()
            }
            simulation.delete()
        }
    }

    init {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        xmlMapper.registerModule(kotlinModule())
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        jsonMapper.registerModules(JavaTimeModule())
    }

    @Test
    fun testStore() {
        val info = storageService.store(simpleNetwork)
        assertEquals(simpleNetwork.documentName, info.documentName)
        assertEquals(info.createdAt, info.lastModifiedAt)
    }

    @Test
    fun testStoreNoEdges() {
        assertThrows<StorageException> {
            storageService.store(noEdgeNetwork)
        }
    }

    @Test
    fun testModify() {
        val initialNetwork = simpleNetwork
        val initialInfo = storageService.store(initialNetwork)
        val initialStoredNetwork = storageService.export(initialInfo.id)
        assertEquals(initialNetwork.documentName, initialNetwork.documentName)
        assertEquals( initialNetwork, initialStoredNetwork)

        val modifiedNetwork = fourWayIntersection
        val modifiedInfo = storageService.store(initialInfo.id, modifiedNetwork)
        val modifiedStoredNetwork = storageService.export(modifiedInfo.id)
        assertEquals(initialInfo.id, modifiedInfo.id)
        assertEquals( initialInfo.createdAt, modifiedInfo.createdAt)
        assertNotEquals(initialInfo.lastModifiedAt, modifiedInfo.lastModifiedAt)
        assertNotEquals(initialStoredNetwork, modifiedStoredNetwork)
    }

    @Test
    fun testRestoreBadModification() {
        val info = storageService.store(simpleNetwork)
        val id = info.id

        assertThrows<StorageException> {
            storageService.store(id, noEdgeNetwork)
        }

        assertEquals(simpleNetwork, storageService.export(id))
        assertNotEquals(noEdgeNetwork, storageService.export(id))
    }

    @Test
    fun testDelete() {
        val info = storageService.store(simpleNetwork)
        storageService.delete(info.id)
        assertThrows<StorageSimulationNotFoundException> {
            storageService.info(info.id)
        }
    }

    @Test
    fun testDeleteNotFound() {
        assertThrows<StorageSimulationNotFoundException> {
            storageService.delete("abcd")
        }
    }

    @Test
    fun testInfoNotFound() {
        assertThrows<StorageSimulationNotFoundException> {
            storageService.info("abcd")
        }
    }

    @Test
    fun testInvalidSimulationId() {
        assertThrows<StorageBadRequestException> {
            storageService.delete("")
        }
        assertThrows<StorageBadRequestException> {
            storageService.info("")
        }
    }

    @Test
    fun testListAll() {
        storageService.store(simpleNetwork)
        storageService.store(fourWayIntersection)
        assertThrows<StorageException> { storageService.store(noEdgeNetwork) }
        val infos = storageService.listAll()
        val documentNames = infos.map { it.documentName }

        assertTrue(infos.count() >= 2)
        assertTrue(simpleNetwork.documentName in documentNames)
        assertTrue(fourWayIntersection.documentName in documentNames)
        assertFalse(noEdgeNetwork.documentName in documentNames)
    }
}