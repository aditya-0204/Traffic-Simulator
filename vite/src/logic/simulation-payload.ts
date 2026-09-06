import { Connection, Edge, Flow, Node, Route, VType } from '~/types/Network';
import { Network } from '~/zustand/useNetworkStore';

type SimulationPayload = {
  documentName: string;
  nodes: Node[];
  edges: Edge[];
  connections: Connection[];
  route: Route[];
  flow: Flow[];
  vType: VType[];
};

const ROUNDABOUT_RADIUS = 68;
const ROUNDABOUT_SEGMENTS = 8;
const FULL_CIRCLE_RADIANS = Math.PI * 2;

const DEFAULT_CAR_TYPE: VType = {
  id: 'car',
  accel: 2.6,
  decel: 4.5,
  sigma: 1,
  length: 5,
  minGap: 2.5,
  maxSpeed: 30,
};

const DEFAULT_AMBULANCE_TYPE: VType = {
  id: 'ambulance',
  accel: 3.2,
  decel: 5.2,
  sigma: 0.6,
  length: 5.5,
  minGap: 2,
  maxSpeed: 35,
};

const DEFAULT_FIRE_BRIGADE_TYPE: VType = {
  id: 'firebrigade',
  accel: 2.8,
  decel: 4.8,
  sigma: 0.7,
  length: 7.5,
  minGap: 2.5,
  maxSpeed: 28,
};

function withDefaultVehicleTypes(vehicleTypes: VType[]) {
  const requiredTypes = [
    DEFAULT_CAR_TYPE,
    DEFAULT_AMBULANCE_TYPE,
    DEFAULT_FIRE_BRIGADE_TYPE,
  ];

  const typeMap = new Map(vehicleTypes.map(vehicleType => [vehicleType.id, vehicleType]));

  for (const vehicleType of requiredTypes) {
    if (!typeMap.has(vehicleType.id)) {
      typeMap.set(vehicleType.id, vehicleType);
    }
  }

  return Array.from(typeMap.values());
}

function createEmergencyFlow(baseFlow: Flow, type: 'ambulance' | 'firebrigade') {
  const scale = type === 'ambulance' ? 0.02 : 0.01;
  const vehsPerHour = Math.max(1, Math.round(baseFlow.vehsPerHour * scale));

  return {
    ...baseFlow,
    id: `${baseFlow.id}_${type}`,
    type,
    vehsPerHour,
  };
}

function withEmergencyFlows(flows: Flow[]) {
  const emergencyFlows: Flow[] = [];

  for (const flow of flows) {
    if (flow.type === 'ambulance' || flow.type === 'firebrigade') {
      continue;
    }

    emergencyFlows.push(createEmergencyFlow(flow, 'ambulance'));
    emergencyFlows.push(createEmergencyFlow(flow, 'firebrigade'));
  }

  return [...flows, ...emergencyFlows];
}

function toSimulationSafeNode(node: Node): Node {
  if (node.type === 'roundabout' || node.type === 'flyover') {
    return {
      ...node,
      // SUMO backend does not support these editor-only node types directly.
      type: 'priority' as const,
    };
  }

  return node;
}

function withSimulationSafeNodeTypes(nodes: Node[]) {
  return nodes.map(node => {
    return toSimulationSafeNode(node);
  });
}

function toPayloadNode(node: Node): Node {
  return {
    id: node.id,
    x: node.x,
    y: node.y,
    type: node.type,
  };
}

type RoundaboutPortal = Node & {
  angle: number;
  neighborId?: string;
};

function getRoundaboutPortalId(roundaboutId: string, neighborId: string) {
  return `${roundaboutId}_roundabout_${neighborId}`;
}

function getRoundaboutEdgeId(fromPortalId: string, toPortalId: string) {
  return `${fromPortalId}_${toPortalId}`;
}

function normalizeAngle(angle: number) {
  return (angle + FULL_CIRCLE_RADIANS) % FULL_CIRCLE_RADIANS;
}

function angularDistance(angleA: number, angleB: number) {
  const distance = Math.abs(angleA - angleB);
  return Math.min(distance, FULL_CIRCLE_RADIANS - distance);
}

function getRoundaboutPortal(
  roundabout: Node,
  neighbor: Node,
): RoundaboutPortal {
  const angle = normalizeAngle(
    Math.atan2(neighbor.y - roundabout.y, neighbor.x - roundabout.x),
  );

  return {
    id: getRoundaboutPortalId(roundabout.id, neighbor.id),
    x: roundabout.x + Math.cos(angle) * ROUNDABOUT_RADIUS,
    y: roundabout.y + Math.sin(angle) * ROUNDABOUT_RADIUS,
    type: 'priority',
    angle,
    neighborId: neighbor.id,
  };
}

function getRoundaboutSupportPortal(roundabout: Node, index: number) {
  const angle = (FULL_CIRCLE_RADIANS / ROUNDABOUT_SEGMENTS) * index;

  return {
    id: `${roundabout.id}_roundabout_ring_${index}`,
    x: roundabout.x + Math.cos(angle) * ROUNDABOUT_RADIUS,
    y: roundabout.y + Math.sin(angle) * ROUNDABOUT_RADIUS,
    type: 'priority' as const,
    angle,
  };
}

function getRoundaboutPath(
  portals: RoundaboutPortal[],
  fromPortalId: string,
  toPortalId: string,
) {
  if (portals.length < 2) {
    return [];
  }

  const fromIndex = portals.findIndex(portal => portal.id === fromPortalId);
  const toIndex = portals.findIndex(portal => portal.id === toPortalId);

  if (fromIndex === -1 || toIndex === -1) {
    return [];
  }

  const path: string[] = [];
  let currentIndex = fromIndex;

  do {
    const nextIndex = (currentIndex + 1) % portals.length;
    path.push(getRoundaboutEdgeId(portals[currentIndex].id, portals[nextIndex].id));
    currentIndex = nextIndex;
  } while (currentIndex !== toIndex);

  return path;
}

function expandRoundaboutsForSimulation(
  nodes: Record<string, Node>,
  edges: Record<string, Edge>,
  routes: Record<string, Route>,
) {
  const roundabouts = Object.values(nodes).filter(
    node => node.type === 'roundabout',
  );

  if (roundabouts.length === 0) {
    return {
      nodes: withSimulationSafeNodeTypes(Object.values(nodes)),
      edges: Object.values(edges),
      routes: Object.values(routes),
    };
  }

  const roundaboutIds = new Set(roundabouts.map(node => node.id));
  const portalsByRoundabout = new Map<string, RoundaboutPortal[]>();

  for (const roundabout of roundabouts) {
    const neighborIds = new Set<string>();

    for (const edge of Object.values(edges)) {
      if (edge.from === roundabout.id && nodes[edge.to]) {
        neighborIds.add(edge.to);
      }

      if (edge.to === roundabout.id && nodes[edge.from]) {
        neighborIds.add(edge.from);
      }
    }

    const portals = Array.from(neighborIds)
      .map(neighborId => getRoundaboutPortal(roundabout, nodes[neighborId]))
      .concat(
        Array.from({ length: ROUNDABOUT_SEGMENTS })
          .map((_, index) => getRoundaboutSupportPortal(roundabout, index))
          .filter(
            supportPortal =>
              !Array.from(neighborIds)
                .map(neighborId =>
                  getRoundaboutPortal(roundabout, nodes[neighborId]),
                )
                .some(
                  neighborPortal =>
                    angularDistance(neighborPortal.angle, supportPortal.angle) <
                    0.08,
                ),
          ),
      )
      .sort((a, b) => a.angle - b.angle);

    portalsByRoundabout.set(roundabout.id, portals);
  }

  const simulationNodes = Object.values(nodes)
    .filter(node => node.type !== 'roundabout')
    .map(toSimulationSafeNode)
    .map(toPayloadNode);

  for (const portals of portalsByRoundabout.values()) {
    simulationNodes.push(...portals.map(toPayloadNode));
  }

  const simulationEdges = Object.values(edges).map(edge => {
    const fromRoundabout = roundaboutIds.has(edge.from);
    const toRoundabout = roundaboutIds.has(edge.to);
    const from = fromRoundabout
      ? getRoundaboutPortalId(edge.from, edge.to)
      : edge.from;
    const to = toRoundabout
      ? getRoundaboutPortalId(edge.to, edge.from)
      : edge.to;

    return {
      ...edge,
      from,
      to,
    };
  });

  for (const portals of portalsByRoundabout.values()) {
    if (portals.length < 2) {
      continue;
    }

    portals.forEach((portal, index) => {
      const nextPortal = portals[(index + 1) % portals.length];

      simulationEdges.push({
        id: getRoundaboutEdgeId(portal.id, nextPortal.id),
        from: portal.id,
        to: nextPortal.id,
        priority: 1,
        numLanes: 1,
        width: 12,
        speed: 8.33,
        name: 'Roundabout',
        spreadType: 'center',
      });
    });
  }

  const simulationRoutes = Object.values(routes).map(route => {
    const routeEdges = route.edges.split(' ').filter(Boolean);
    const expandedEdges: string[] = [];

    routeEdges.forEach((edgeId, index) => {
      const edge = edges[edgeId];
      const nextEdge = edges[routeEdges[index + 1]];

      expandedEdges.push(edgeId);

      if (!edge || !nextEdge || edge.to !== nextEdge.from || !roundaboutIds.has(edge.to)) {
        return;
      }

      const portals = portalsByRoundabout.get(edge.to) ?? [];
      const fromPortalId = getRoundaboutPortalId(edge.to, edge.from);
      const toPortalId = getRoundaboutPortalId(edge.to, nextEdge.to);

      expandedEdges.push(...getRoundaboutPath(portals, fromPortalId, toPortalId));
    });

    return {
      ...route,
      edges: expandedEdges.join(' '),
    };
  });

  return {
    nodes: simulationNodes,
    edges: simulationEdges,
    routes: simulationRoutes,
  };
}

function connectionsFromRoutes(routes: Route[]): Connection[] {
  const connections = new Map<string, Connection>();

  for (const route of routes) {
    const routeEdges = route.edges.split(' ').filter(Boolean);

    for (let index = 0; index < routeEdges.length - 1; index += 1) {
      const from = routeEdges[index];
      const to = routeEdges[index + 1];
      const id = `${from}_${to}_0_0`;

      connections.set(id, {
        from,
        to,
        fromLane: 0,
        toLane: 0,
      });
    }
  }

  return Array.from(connections.values());
}

export function buildSimulationPayload(network: Network): SimulationPayload {
  const flows = Object.values(network.flow);
  const vehicleTypes = Object.values(network.vType);
  const expandedNetwork = expandRoundaboutsForSimulation(
    network.nodes,
    network.edges,
    network.route,
  );
  const generatedConnections = connectionsFromRoutes(expandedNetwork.routes);

  return {
    documentName: network.documentName,
    nodes: expandedNetwork.nodes,
    edges: expandedNetwork.edges,
    connections:
      generatedConnections.length > 0
        ? generatedConnections
        : Object.values(network.connections),
    route: expandedNetwork.routes,
    flow: withEmergencyFlows(flows),
    vType: withDefaultVehicleTypes(vehicleTypes),
  };
}
