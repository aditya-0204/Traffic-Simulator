import { buildSimulationPayload } from '../simulation-payload';

describe('buildSimulationPayload', () => {
  it('adds ambulance and fire brigade vehicle types and flows', () => {
    const payload = buildSimulationPayload({
      documentName: 'Test Network',
      nodes: {
        A: { id: 'A', x: 0, y: 0, type: 'priority' },
        B: { id: 'B', x: 10, y: 0, type: 'priority' },
      },
      edges: {
        A_B: {
          id: 'A_B',
          from: 'A',
          to: 'B',
          priority: 1,
          numLanes: 1,
          width: 3,
          speed: 13.89,
          name: 'Main Road',
        },
      },
      connections: {
        A_B_A_B_0_0: {
          from: 'A_B',
          to: 'A_B',
          fromLane: 0,
          toLane: 0,
        },
      },
      vType: {},
      route: {
        route_1: {
          id: 'route_1',
          edges: 'A_B',
        },
      },
      flow: {
        flow_1: {
          id: 'flow_1',
          type: 'car',
          route: 'route_1',
          begin: 0,
          end: 3600,
          vehsPerHour: 1000,
        },
      },
      setDocumentName: () => {},
      addNode: () => {},
      updateNode: () => {},
      drawEdge: () => {},
      updateEdge: () => {},
      deleteNode: () => {},
      deleteEdge: () => {},
      addConnection: () => {},
      updateFlow: () => {},
      clearNetwork: () => {},
    });

    expect(payload.vType.map(vehicleType => vehicleType.id)).toEqual(
      expect.arrayContaining(['car', 'ambulance', 'firebrigade']),
    );

    expect(payload.flow).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          id: 'flow_1_ambulance',
          type: 'ambulance',
          route: 'route_1',
          vehsPerHour: 20,
        }),
        expect.objectContaining({
          id: 'flow_1_firebrigade',
          type: 'firebrigade',
          route: 'route_1',
          vehsPerHour: 10,
        }),
      ]),
    );
  });

  it('converts flyovers to simulation-safe node types', () => {
    const payload = buildSimulationPayload({
      documentName: 'Roundabout Network',
      nodes: {
        A: { id: 'A', x: 0, y: 0, type: 'flyover' },
        B: { id: 'B', x: 10, y: 0, type: 'priority' },
      },
      edges: {},
      connections: {},
      vType: {},
      route: {},
      flow: {},
      setDocumentName: () => {},
      addNode: () => {},
      updateNode: () => {},
      drawEdge: () => {},
      updateEdge: () => {},
      deleteNode: () => {},
      deleteEdge: () => {},
      addConnection: () => {},
      updateFlow: () => {},
      clearNetwork: () => {},
    });

    expect(payload.nodes).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ id: 'A', type: 'priority' }),
        expect.objectContaining({ id: 'B', type: 'priority' }),
      ]),
    );
  });

  it('expands roundabouts into circular simulation routes', () => {
    const payload = buildSimulationPayload({
      documentName: 'Roundabout Network',
      nodes: {
        A: { id: 'A', x: 0, y: 100, type: 'priority' },
        R: { id: 'R', x: 100, y: 100, type: 'roundabout' },
        B: { id: 'B', x: 200, y: 100, type: 'priority' },
      },
      edges: {
        A_R: {
          id: 'A_R',
          from: 'A',
          to: 'R',
          priority: 1,
          numLanes: 1,
          width: 3,
          speed: 13.89,
          name: 'Road In',
        },
        R_B: {
          id: 'R_B',
          from: 'R',
          to: 'B',
          priority: 1,
          numLanes: 1,
          width: 3,
          speed: 13.89,
          name: 'Road Out',
        },
      },
      connections: {
        A_R_R_B_0_0: {
          from: 'A_R',
          to: 'R_B',
          fromLane: 0,
          toLane: 0,
        },
      },
      vType: {},
      route: {
        route_1: {
          id: 'route_1',
          edges: 'A_R R_B',
        },
      },
      flow: {
        flow_1: {
          id: 'flow_1',
          type: 'car',
          route: 'route_1',
          begin: 0,
          end: 3600,
          vehsPerHour: 1000,
        },
      },
      setDocumentName: () => {},
      addNode: () => {},
      updateNode: () => {},
      drawEdge: () => {},
      updateEdge: () => {},
      deleteNode: () => {},
      deleteEdge: () => {},
      addConnection: () => {},
      updateFlow: () => {},
      clearNetwork: () => {},
    });

    expect(payload.nodes).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ id: 'R_roundabout_A', type: 'priority' }),
        expect.objectContaining({ id: 'R_roundabout_B', type: 'priority' }),
        expect.objectContaining({ id: 'R_roundabout_ring_5', type: 'priority' }),
      ]),
    );

    expect(payload.nodes).not.toEqual(
      expect.arrayContaining([expect.objectContaining({ id: 'R' })]),
    );
    expect(payload.nodes).toEqual(
      payload.nodes.map(node => ({
        id: node.id,
        x: node.x,
        y: node.y,
        type: node.type,
      })),
    );

    expect(payload.edges).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ id: 'A_R', from: 'A', to: 'R_roundabout_A' }),
        expect.objectContaining({ id: 'R_B', from: 'R_roundabout_B', to: 'B' }),
        expect.objectContaining({
          id: 'R_roundabout_A_R_roundabout_ring_5',
          from: 'R_roundabout_A',
          to: 'R_roundabout_ring_5',
        }),
      ]),
    );

    const route = payload.route.find(item => item.id === 'route_1');

    expect(route?.edges).toBe(
      'A_R R_roundabout_A_R_roundabout_ring_5 R_roundabout_ring_5_R_roundabout_ring_6 R_roundabout_ring_6_R_roundabout_ring_7 R_roundabout_ring_7_R_roundabout_B R_B',
    );
  });
});
