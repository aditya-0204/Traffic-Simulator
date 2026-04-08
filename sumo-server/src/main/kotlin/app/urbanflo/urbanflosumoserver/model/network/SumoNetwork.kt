package app.urbanflo.urbanflosumoserver.model.network

import app.urbanflo.urbanflosumoserver.model.SimulationInfo
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

/**
 * SUMO network data, in the format used by the frontend.
 */
data class SumoNetwork(
    /**
     * Document name
     */
    @field:NotBlank(message = "Document name cannot be blank")
    val documentName: String,
    /**
     * List of nodes
     */
    @field:NotEmpty(message = "Nodes must not be empty")
    val nodes: List<SumoNode>,
    /**
     * List of edges
     */
    @field:NotEmpty(message = "Edges must not be empty")
    @field:Valid
    val edges: List<SumoEdge>,
    /**
     * List of connections
     */
    @field:NotEmpty(message = "Connections must not be empty")
    val connections: List<SumoConnection>,
    /**
     * List of vehicle types (as `vType` in the JSON)
     */
    @field:JsonProperty("vType")
    val vehicleType: List<SumoVehicleType>,
    /**
     * List of routes
     */
    val route: List<SumoRoute>,
    /**
     * List of flows
     */
    val flow: List<SumoFlow>
) {
    constructor(
        simulationInfo: SimulationInfo,
        nodesXml: SumoNodesXml,
        edgesXml: SumoEdgesXml,
        connectionsXml: SumoConnectionsXml,
        routesXml: SumoRoutesXml
    ) : this(
        simulationInfo.documentName,
        nodesXml.nodes,
        edgesXml.edges,
        connectionsXml.connections,
        routesXml.vehicleTypes,
        routesXml.routes,
        routesXml.flows
    )

    /**
     * Returns a node description compatible with SUMO's [node description XML.](https://sumo.dlr.de/docs/Networks/PlainXML.html#node_descriptions)
     */
    fun nodesXml() = SumoNodesXml(this.nodes)
    /**
     * Returns an edge description compatible with SUMO's [edge description XML.](https://sumo.dlr.de/docs/Networks/PlainXML.html#edge_descriptions)
     */
    fun edgesXml() = SumoEdgesXml(this.edges)
    /**
     * Returns a connection description compatible with SUMO's [connection description XML.](https://sumo.dlr.de/docs/Networks/PlainXML.html#connection_descriptions)
     */
    fun connectionsXml() = SumoConnectionsXml(this.connections)
    /**
     * Returns a route description compatible with SUMO's [route description XML.](https://sumo.dlr.de/docs/Definition_of_Vehicles%2C_Vehicle_Types%2C_and_Routes.html#vehicles_and_routes)
     */
    fun routesXml() = SumoRoutesXml(
        this.vehicleType,
        this.route,
        this.flow
    )
}
