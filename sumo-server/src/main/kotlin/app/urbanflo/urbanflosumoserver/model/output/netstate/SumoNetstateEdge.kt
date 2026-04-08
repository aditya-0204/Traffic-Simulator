package app.urbanflo.urbanflosumoserver.model.output.netstate

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * Time step item for [SumoNetstateXml].
 */
data class SumoNetstateEdge(
    val id: String,
    @field:JacksonXmlProperty(localName = "lane")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val lanes: List<SumoNetstateLane>
)
