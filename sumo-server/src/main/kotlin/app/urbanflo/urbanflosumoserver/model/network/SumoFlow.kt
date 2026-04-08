package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SumoEntityId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * SUMO [flow.](https://sumo.dlr.de/docs/Definition_of_Vehicles%2C_Vehicle_Types%2C_and_Routes.html#repeated_vehicles_flows)
 */
data class SumoFlow(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val type: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val route: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val begin: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val end: Double,
    @field:JacksonXmlProperty(isAttribute = true)
    val vehsPerHour: Double
)
