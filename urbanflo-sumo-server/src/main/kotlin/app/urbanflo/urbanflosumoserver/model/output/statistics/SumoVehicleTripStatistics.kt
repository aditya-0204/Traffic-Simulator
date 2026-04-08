package app.urbanflo.urbanflosumoserver.model.output.statistics

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoStatisticsXml]
 */
data class SumoVehicleTripStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    val count: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val routeLength: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val speed: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val duration: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val waitingTime: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val timeLoss: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val departDelay: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val departDelayWaiting: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val totalTravelTime: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val totalDepartDelay: Double
)
