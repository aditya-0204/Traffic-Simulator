package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.annotation.JsonValue

/**
 * SUMO [spread type](https://sumo.dlr.de/docs/Networks/PlainXML.html#spreadtype).
 */
enum class SumoSpreadType(@JsonValue val spreadType: String) {
    RIGHT("right"),
    CENTER("center"),
    ROAD_CENTER("roadCenter")
}