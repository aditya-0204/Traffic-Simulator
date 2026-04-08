package app.urbanflo.urbanflosumoserver.model.output.tripinfo

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * @see [SumoTripInfoXml]
 */
data class SumoTripInfo(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val depart: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val departLane: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val departPos: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val departSpeed: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val departDelay: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrival: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrivalLane: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrivalPos: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val arrivalSpeed: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val duration: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val routeLength: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val waitingTime: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val waitingCount: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val stopTime: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val timeLoss: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val rerouteNo: Int,
    @field:JacksonXmlProperty(isAttribute = true)
    val devices: String,
    @field:JacksonXmlProperty(isAttribute = true, localName = "vType")
    val vehicleType: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val speedFactor: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val vaporized: String?
)
