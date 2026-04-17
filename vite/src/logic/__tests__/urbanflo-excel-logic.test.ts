import { buildUrbanfloExcelWorkbook } from '../urbanflo-excel-logic';

describe('buildUrbanfloExcelWorkbook', () => {
  it('includes dataset-oriented trip and netstate worksheets', () => {
    const workbook = buildUrbanfloExcelWorkbook(
      JSON.stringify({
        version: 1,
        networkData: {
          documentName: 'Dataset Demo',
          nodes: {},
          edges: {},
          connections: {},
          vType: {},
          route: {},
          flow: {},
        },
        decorations: {
          items: [],
        },
        simulationHistory: [
          {
            startTime: '2026-04-11T10:00:00.000Z',
            endTime: '2026-04-11T10:05:00.000Z',
            simulation: {
              info: {
                id: 'sim-1',
                documentName: 'Dataset Demo',
                createdAt: '2026-04-11T10:00:00.000Z',
                lastModifiedAt: '2026-04-11T10:05:00.000Z',
              },
              analytics: {},
              statistics: {
                performance: {
                  clockBegin: '2026-04-11T10:00:00.000Z',
                  clockEnd: '2026-04-11T10:05:00.000Z',
                  clockDuration: 300,
                },
                vehicles: {
                  loaded: 10,
                  inserted: 10,
                  running: 0,
                  waiting: 1,
                },
                teleports: {
                  total: 0,
                  jam: 0,
                  yield: 0,
                  wrongLane: 0,
                },
                safety: {
                  collisions: 0,
                  emergencyStops: 0,
                  emergencyBraking: 0,
                },
                vehicleTripStatistics: {
                  count: 10,
                  routeLength: 1000,
                  speed: 12.5,
                  duration: 80,
                  waitingTime: 10,
                  timeLoss: 5,
                  departDelay: 0,
                  departDelayWaiting: 0,
                  totalTravelTime: 800,
                  totalDepartDelay: 0,
                },
              },
              output: {
                tripInfo: [
                  {
                    id: 'ambulance-1',
                    depart: 0,
                    departLane: 'lane-1',
                    departPos: 0,
                    departSpeed: 5,
                    departDelay: 0,
                    arrival: 100,
                    arrivalLane: 'lane-2',
                    arrivalPos: 200,
                    arrivalSpeed: 10,
                    duration: 100,
                    routeLength: 1200,
                    waitingTime: 5,
                    waitingCount: 1,
                    stopTime: 0,
                    timeLoss: 2,
                    devices: '',
                    vehicleType: 'ambulance',
                    speedFactor: 1,
                    vaporized: null,
                  },
                ],
                netstate: [
                  {
                    time: 1,
                    edges: [
                      {
                        id: 'edge-1',
                        lanes: [
                          {
                            id: 'lane-1',
                            vehicles: [
                              {
                                id: 'ambulance-1',
                                pos: 42,
                                speed: 14.2,
                              },
                            ],
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
            },
          },
        ],
      }),
    );

    expect(workbook).toContain('TripInfoDataset');
    expect(workbook).toContain('NetstateDataset');
    expect(workbook).toContain('StatisticsDataset');
    expect(workbook).toContain('vehicleId');
    expect(workbook).toContain('vehicleType');
    expect(workbook).toContain('departSpeed');
    expect(workbook).toContain('arrivalSpeed');
    expect(workbook).toContain('laneId');
    expect(workbook).toContain('pos');
    expect(workbook).toContain('speed');
    expect(workbook).toContain('ambulance-1');
  });
});
