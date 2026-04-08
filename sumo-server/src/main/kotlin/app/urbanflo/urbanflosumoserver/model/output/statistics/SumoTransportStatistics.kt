package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoStatisticsXml]
 */
data class SumoTransportStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val number: Int
)
