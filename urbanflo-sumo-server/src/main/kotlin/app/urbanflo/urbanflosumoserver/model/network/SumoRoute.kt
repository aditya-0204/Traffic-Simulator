package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SumoEntityId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * SUMO [route.](https://sumo.dlr.de/docs/Definition_of_Vehicles%2C_Vehicle_Types%2C_and_Routes.html#routes)
 */
data class SumoRoute(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: SumoEntityId,
    @field:JacksonXmlProperty(isAttribute = true)
    val edges: SumoEntityId
)
