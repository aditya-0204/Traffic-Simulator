package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SumoEntityId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero

/**
 * @see [SumoEdgesXml]
 */
data class SumoEdge(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val from: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val to: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val priority: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    @field:Positive(message = "numLanes must be a positive value")
    val numLanes: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    @field:PositiveOrZero(message = "speed cannot be negative")
    val speed: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val width: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val name: String?,
    @field:JacksonXmlProperty(isAttribute = true)
    val spreadType: SumoSpreadType?,
)