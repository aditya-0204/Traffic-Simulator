package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoStatisticsXml]
 */
data class SumoPersonTeleports(
    @field:JacksonXmlProperty(isAttribute = true)
    val total: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val abortWait: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val wrongDest: Int
)
