package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

/**
 * Data class representing [connection description XML.](https://sumo.dlr.de/docs/Networks/PlainXML.html#connection_descriptions)
 */
@JacksonXmlRootElement(localName = "connections")
data class SumoConnectionsXml(
    @field:JacksonXmlProperty(localName = "connection")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val connections: List<SumoConnection> = listOf()
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.con.xml"
    }
}
