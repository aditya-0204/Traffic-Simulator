package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @See [SumoStatisticsXml]
 */
data class SumoSafetyStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val collisions: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val emergencyStops: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val emergencyBraking: Int,
)
