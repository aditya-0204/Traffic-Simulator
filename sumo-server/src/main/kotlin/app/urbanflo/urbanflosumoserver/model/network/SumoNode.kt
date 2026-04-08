package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SumoEntityId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoNodesXml]
 */
data class SumoNode(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val x: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val y: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val type: SumoNodeType
)
