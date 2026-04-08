package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoStatisticsXml]
 */
data class SumoPedestrianStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val number: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val routeLength: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val duration: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val timeLoss: Double
)
