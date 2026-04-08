package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @See [SumoStatisticsXml]
 */
data class SumoTeleportsStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val total: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val jam: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val yield: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val wrongLane: Int
)