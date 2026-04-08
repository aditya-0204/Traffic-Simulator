package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.model.ErrorResponse
import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import app.urbanflo.urbanflosumoserver.model.network.SumoNetwork
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.io.ClassPathResource
import org.springframework.http.*
import java.io.File

/**
 * Tests for controller APIs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiTests(@Autowired private val restTemplate: TestRestTemplate) {
    @Value(value = "\${local.server.port}")
    private val port = 0

    @Value("\${urbanflo.storage.location:uploads}")
    lateinit var uploadsLocation: String

    private val xmlMapper = XmlMapper()
    private val jsonMapper = jacksonObjectMapper()

    val simpleNetwork = ClassPathResource("simple-network.json").file.readText()
    val fourWayIntersection = ClassPathResource("4-way-intersection.json").file.readText()
    val noEdgeNetwork = ClassPathResource("no-edges.json").file.readText()
    val missingFields = ClassPathResource("missing-fields.json").file.readText()
    val invalidEdges = ClassPathResource("invalid-edges.json").file.readText()

    init {
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        xmlMapper.registerModule(kotlinModule())
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        jsonMapper.registerModules(JavaTimeModule())
    }

    @BeforeEach
    fun deleteAllUploads() {
        File(uploadsLocation).listFiles()?.forEach { simulation ->
            simulation.listFiles()?.forEach { file ->
                file.delete()
            }
            simulation.delete()
        }
    }

    @Test
    fun testUploadNetwork() {
        val request = HttpEntity(simpleNetwork, httpHeaders)
        val response: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", request)
        assertEquals(HttpStatus.CREATED, response.statusCode)

        val info = response.body!!
        val network: SumoNetwork = jsonMapper.readValue(simpleNetwork)
        assertEquals(network.documentName, info.documentName)

        // test simulation info
        val infoRequest: HttpEntity<SimulationInfo> = HttpEntity(httpHeaders)
        val infoResponse: ResponseEntity<SimulationInfo> =
            restTemplate.getForEntity("http://localhost:${port}/simulation/${info.id}", infoRequest)
        assertEquals(HttpStatus.OK, infoResponse.statusCode)
        assertEquals(info.id, infoResponse.body!!.id)
        assertEquals(info.documentName, infoResponse.body!!.documentName)

        // test simulation network
        val networkRequest: HttpEntity<SumoNetwork> = HttpEntity(httpHeaders)
        val networkResponse: ResponseEntity<SumoNetwork> =
            restTemplate.getForEntity("http://localhost:${port}/simulation/${info.id}/network", networkRequest)
        assertEquals(HttpStatus.OK, networkResponse.statusCode)
        assertEquals(network, networkResponse.body!!)
    }

    @Test
    fun testInvalidDocumentName() {
        val baseNetworkObject = jsonMapper.readValue<SumoNetwork>(simpleNetwork)
        val documentNames = arrayOf("", " ", "\n", "\r\n", "\u0000", "\t")
        documentNames.forEach { name ->
            val networkObject = SumoNetwork(
                name,
                baseNetworkObject.nodes,
                baseNetworkObject.edges,
                baseNetworkObject.connections,
                baseNetworkObject.vehicleType,
                baseNetworkObject.route,
                baseNetworkObject.flow
            )
            val network = jsonMapper.writeValueAsString(networkObject)
            val request = HttpEntity(network, httpHeaders)
            val response: ResponseEntity<ErrorResponse> =
                restTemplate.postForEntity("http://localhost:${port}/simulation", request)
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertTrue("documentName" in response.body!!.errorFields!!.keys)
        }
    }

    /**
     * Test document names that are technically valid (i.e. not blank and is valid JSON string) but may pose problems
     * in the frontend depending on the platform
     */
    @Test
    fun testProblematicDocumentNames() {
        val baseNetworkObject = jsonMapper.readValue<SumoNetwork>(simpleNetwork)
        val documentNames = arrayOf(
            // non-ascii characters
            "Безымянный документ",
            "وثيقة بدون عنوان",
            "Untitled Document وثيقة بدون عنوان Безымянный докумен",
            // emojis
            "\uD83D\uDE02", // 😂
            "\uD83C\uDDE6\uD83C\uDDFA", // 🇦🇺 -- flag of Australia
            // names containing forbidden windows filenames
            // https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file
            "Untitled Document 1\\10",
            "Untitled Document.",
            "Untitled Document?",
            "Untitled Document ",
            "Untitled Document 2: Electric Boogaloo",
            "con",
            "Con",
            "CON",
            "A".repeat(1000),
            // other problematic filesystem document names
            "Untitled Document 1/10",
            "Untitled Document\u0000",
            ".",
            "..",
            ".Untitled",
            // other
            "\"Untitled Document\"",
            "\\n",
            "\\0"
        )
        documentNames.forEach { name ->
            val networkObject = SumoNetwork(
                name,
                baseNetworkObject.nodes,
                baseNetworkObject.edges,
                baseNetworkObject.connections,
                baseNetworkObject.vehicleType,
                baseNetworkObject.route,
                baseNetworkObject.flow
            )
            val network = jsonMapper.writeValueAsString(networkObject)
            val request = HttpEntity(network, httpHeaders)
            val response: ResponseEntity<SimulationInfo> =
                restTemplate.postForEntity("http://localhost:${port}/simulation", request)
            assertEquals(HttpStatus.CREATED, response.statusCode)
            assertEquals(name, response.body!!.documentName) // test response decoding
        }
    }

    @Test
    fun testMissingFields() {
        val request = HttpEntity(missingFields, httpHeaders)
        val response: ResponseEntity<ErrorResponse> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", request)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun testInvalidNetworkError() {
        val request = HttpEntity(noEdgeNetwork, httpHeaders)
        val response: ResponseEntity<ErrorResponse> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", request)
        // at the moment, we can't distinguish between bad networks and other netconvert errors
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue("edges" in response.body!!.errorFields!!.keys)
    }

    @Test
    fun testInvalidEdges() {
        val request = HttpEntity(invalidEdges, httpHeaders)
        val response: ResponseEntity<ErrorResponse> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", request)
        // at the moment, we can't distinguish between bad networks and other netconvert errors
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue("edges[0].numLanes" in response.body!!.errorFields!!.keys)
        assertTrue("edges[0].speed" in response.body!!.errorFields!!.keys)
    }


    @Test
    fun testModifyNetwork() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val initialResponse: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", initialRequest)
        val id = initialResponse.body!!.id
        val modifyRequest = HttpEntity(fourWayIntersection, httpHeaders)
        val modifyResponse: ResponseEntity<SimulationInfo> =
            restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.PUT, modifyRequest)
        assertEquals(HttpStatus.OK, modifyResponse.statusCode)
        assertEquals(id, modifyResponse.body!!.id)
    }


    @Test
    fun testModifyMissingFields() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val initialResponse: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", initialRequest)
        val id = initialResponse.body!!.id
        val modifyRequest = HttpEntity(missingFields, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> =
            restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.PUT, modifyRequest)
        assertEquals(HttpStatus.BAD_REQUEST, modifyResponse.statusCode)
    }

    @Test
    fun testModifyInvalidNetworkError() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val initialResponse: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", initialRequest)
        val id = initialResponse.body!!.id
        val modifyRequest = HttpEntity(noEdgeNetwork, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> =
            restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.PUT, modifyRequest)
        assertEquals(HttpStatus.BAD_REQUEST, modifyResponse.statusCode)
        assertTrue("edges" in modifyResponse.body!!.errorFields!!.keys)
    }

    @Test
    fun testModifyNotFound() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        restTemplate.postForEntity<SimulationInfo>("http://localhost:${port}/simulation", initialRequest)
        val modifyRequest = HttpEntity(fourWayIntersection, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> =
            restTemplate.exchange("http://localhost:${port}/simulation/12345", HttpMethod.PUT, modifyRequest)
        assertEquals(HttpStatus.NOT_FOUND, modifyResponse.statusCode)
    }

    @Test
    fun testModifyEmptyId() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val id = ""
        restTemplate.postForEntity<SimulationInfo>("http://localhost:${port}/simulation", initialRequest)
        val modifyRequest = HttpEntity(fourWayIntersection, httpHeaders)
        val modifyResponse: ResponseEntity<ErrorResponse> =
            restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.PUT, modifyRequest)
        // this is a 404 since there are no PUT endpoints in /simulation/
        assertEquals(HttpStatus.NOT_FOUND, modifyResponse.statusCode)
    }

    @Test
    fun testDeleteNetwork() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        val initialResponse: ResponseEntity<SimulationInfo> =
            restTemplate.postForEntity("http://localhost:${port}/simulation", initialRequest)
        val id = initialResponse.body!!.id
        val deleteRequest: HttpEntity<Any> = HttpEntity(httpHeaders)
        val deleteResponse: ResponseEntity<Any> =
            restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.DELETE, deleteRequest)
        assertEquals(HttpStatus.OK, deleteResponse.statusCode)

        // any subsequent deletion should return a 404
        val deleteResponse2: ResponseEntity<Any> =
            restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.DELETE, deleteRequest)
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse2.statusCode)
    }

    @Test
    fun testDeleteEmptyId() {
        val initialRequest = HttpEntity(simpleNetwork, httpHeaders)
        restTemplate.postForEntity<SimulationInfo>("http://localhost:${port}/simulation", initialRequest)
        val id = ""
        val deleteRequest: HttpEntity<Any> = HttpEntity(httpHeaders)
        val deleteResponse: ResponseEntity<Any> =
            restTemplate.exchange("http://localhost:${port}/simulation/$id", HttpMethod.DELETE, deleteRequest)
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.statusCode)
    }

    @Test
    fun testInfoNotFound() {
        val id = "12345"
        val request: HttpEntity<Any> = HttpEntity(httpHeaders)
        val deleteResponse: ResponseEntity<Any> =
            restTemplate.getForEntity("http://localhost:${port}/simulation/$id", request)
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.statusCode)
    }

    @Test
    fun testInfoEmptyId() {
        val id = ""
        val request: HttpEntity<Any> = HttpEntity(httpHeaders)
        val deleteResponse: ResponseEntity<Any> =
            restTemplate.getForEntity("http://localhost:${port}/simulation/$id", request)
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.statusCode)
    }

    @Test
    fun testAllInfo() {
        val networks = arrayOf(simpleNetwork, fourWayIntersection, noEdgeNetwork)
        networks.forEach { network ->
            val request = HttpEntity(network, httpHeaders)
            restTemplate.postForEntity<Any>("http://localhost:${port}/simulation", request)
        }
        val request: HttpEntity<List<SimulationInfo>> = HttpEntity(httpHeaders)
        val response: ResponseEntity<List<SimulationInfo>> =
            restTemplate.getForEntity("http://localhost:${port}/simulations", request)

        // I have no idea if jackson is blind or what, but it straight up ignored the type annotation of SimulationInfo
        // and just parses them as LinkedHashMap.
        // Oh, and don't get me started on how long I took to figure out instantiating TypeReference in kotlin, because
        // intellij can't figure out what I wanted to do and the syntax is so obtuse.
        // More info:
        // https://www.baeldung.com/jackson-linkedhashmap-cannot-be-cast
        // https://stackoverflow.com/a/52238965
        val typeReference = object : TypeReference<List<SimulationInfo>>() {}
        val documentNames: List<String> =
            jsonMapper.convertValue(response.body!!, typeReference).map { it.documentName }
        val simpleNetworkObject: SumoNetwork = jsonMapper.readValue(simpleNetwork)
        val fourWayIntersectionObject: SumoNetwork = jsonMapper.readValue(fourWayIntersection)
        val noEdgeNetworkObject: SumoNetwork = jsonMapper.readValue(noEdgeNetwork)

        assertTrue(simpleNetworkObject.documentName in documentNames)
        assertTrue(fourWayIntersectionObject.documentName in documentNames)
        assertFalse(noEdgeNetworkObject.documentName in documentNames)
    }


    companion object {
        @JvmStatic
        private lateinit var httpHeaders: HttpHeaders

        @JvmStatic
        @BeforeAll
        fun initHttpHeaders() {
            httpHeaders = HttpHeaders()
            httpHeaders.contentType = MediaType.APPLICATION_JSON
        }
    }
}