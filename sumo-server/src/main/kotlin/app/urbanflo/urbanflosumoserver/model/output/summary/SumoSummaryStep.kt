package app.urbanflo.urbanflosumoserver.model.output.summary

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoSummaryXml]
 */
data class SumoSummaryStep(
    @field:JacksonXmlProperty(isAttribute = true)
    val time: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val loaded: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val inserted: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val running: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val waiting: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val ended: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrived: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val collisions: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val teleports: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val halting: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val stopped: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val meanWaitingTime: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val meanTravelTime: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val meanSpeed: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val meanSpeedRelative: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val duration: Long
)
