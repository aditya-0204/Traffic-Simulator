package app.urbanflo.urbanflosumoserver.model.network

import com.fasterxml.jackson.annotation.JsonValue

/**
 * SUMO [node type.](https://sumo.dlr.de/docs/Networks/PlainXML.html#node_types)
 */
enum class SumoNodeType(@JsonValue val nodeType: String) {
    PRIORITY("priority"),
    TRAFFIC_LIGHT("traffic_light"),
    RIGHT_BEFORE_LEFT("right_before_left"),
    LEFT_BEFORE_RIGHT("left_before_right"),
    UNREGULATED("unregulated"),
    PRIORITY_STOP("priority_stop"),
    TRAFFIC_LIGHT_UNREGULATED("traffic_light_unregulated"),
    ALLWAY_STOP("allway_stop"),
    RAIL_SIGNAL("rail_signal"),
    ZIPPER("zipper"),
    TRAFFIC_LIGHT_RIGHT_ON_RED("traffic_light_right_on_red"),
    RAIL_CROSSING("rail_crossing"),
    DEAD_END("dead_end")
}