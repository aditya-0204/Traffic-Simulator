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
          vehsPerHour: 100,
        }),
        expect.objectContaining({
          id: 'flow_1_firebrigade',
          type: 'firebrigade',
          route: 'route_1',
          vehsPerHour: 100,
        }),
      ]),
    );
  });
});
