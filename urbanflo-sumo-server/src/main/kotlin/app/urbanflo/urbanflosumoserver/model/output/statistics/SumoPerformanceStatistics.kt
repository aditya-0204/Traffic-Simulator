package app.urbanflo.urbanflosumoserver.model.output.statistics

import app.urbanflo.urbanflosumoserver.jackson.UnixDoubleTimestampDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.OffsetDateTime

/**
 * @see [SumoStatisticsXml]
 */
data class SumoPerformanceStatistics(
    @field:JacksonXmlProperty(isAttribute = true)
    @field:JsonDeserialize(using = UnixDoubleTimestampDeserializer::class)
    val clockBegin: OffsetDateTime,
    @field:JacksonXmlProperty(isAttribute = true)
    @field:JsonDeserialize(using = UnixDoubleTimestampDeserializer::class)
    val clockEnd: OffsetDateTime,
    @field:JacksonXmlProperty(isAttribute = true)
    val clockDuration: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val traciDuration: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val realTimeFactor: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val vehicleUpdatesPerSecond: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val personUpdatesPerSecond: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val begin: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val end: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val duration: Double
)
