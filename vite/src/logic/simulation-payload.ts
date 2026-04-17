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

export function buildSimulationPayload(network: Network): SimulationPayload {
  const flows = Object.values(network.flow);
  const vehicleTypes = Object.values(network.vType);

  return {
    documentName: network.documentName,
    nodes: Object.values(network.nodes),
    edges: Object.values(network.edges),
    connections: Object.values(network.connections),
    route: Object.values(network.route),
    flow: withEmergencyFlows(flows),
    vType: withDefaultVehicleTypes(vehicleTypes),
  };
}
