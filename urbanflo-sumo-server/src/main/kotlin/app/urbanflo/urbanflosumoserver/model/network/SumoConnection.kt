package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SumoEntityId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoConnectionsXml]
 */
data class SumoConnection(
    @field:JacksonXmlProperty(isAttribute = true)
    val from: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val to: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val fromLane: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val toLane: Int
)
