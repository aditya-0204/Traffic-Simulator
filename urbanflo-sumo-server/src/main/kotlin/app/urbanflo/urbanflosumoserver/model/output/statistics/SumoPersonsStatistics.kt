package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoStatisticsXml]
 */
data class SumoPersonsStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val loaded: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val running: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val jammed: Int
)