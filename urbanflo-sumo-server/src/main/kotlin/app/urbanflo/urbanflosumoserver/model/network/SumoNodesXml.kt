package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.simulation.SimulationId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.nio.file.Path

/**
 * Data class representing SUMO's [node description XML.](https://sumo.dlr.de/docs/Networks/PlainXML.html#node_descriptions)
 */
@JacksonXmlRootElement(localName = "nodes")
data class SumoNodesXml(
    @field:JacksonXmlProperty(localName = "node")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    // assign a default empty list to prevent XML empty tag to be deserialized as null
    val nodes: List<SumoNode> = listOf()
) {
    companion object {
        fun filePath(simulationId: SimulationId, simulationDir: Path): Path =
            simulationDir.resolve(fileName(simulationId)).normalize().toAbsolutePath()

        fun fileName(simulationId: SimulationId) = "$simulationId.nod.xml"
    }
}