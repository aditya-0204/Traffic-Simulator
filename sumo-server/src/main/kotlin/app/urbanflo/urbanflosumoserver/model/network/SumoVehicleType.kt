package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SumoEntityId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * SUMO [vehicle type.](https://sumo.dlr.de/docs/Definition_of_Vehicles%2C_Vehicle_Types%2C_and_Routes.html#vehicle_types)
 */
data class SumoVehicleType(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val accel: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val decel: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val sigma: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val length: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val minGap: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val maxSpeed: Double
)
