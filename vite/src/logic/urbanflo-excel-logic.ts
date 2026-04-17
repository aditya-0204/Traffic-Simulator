import { getNetworkFromUploadedFile, geturbanfloFileContents } from './urbanflo-file-logic';

type ExcelCellValue = string | number | boolean | null | undefined;
type ExcelRow = Record<string, ExcelCellValue>;
type ExcelSheet = {
  name: string;
  rows: ExcelRow[];
};

function escapeXml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

function sanitizeWorksheetName(name: string): string {
  return name.replace(/[\\/:*?[\]]/g, ' ').trim().slice(0, 31) || 'Sheet1';
}

function getCellType(value: ExcelCellValue): 'Number' | 'String' {
  return typeof value === 'number' && Number.isFinite(value) ? 'Number' : 'String';
}

function stringifyCellValue(value: ExcelCellValue): string {
  if (value === null || value === undefined) {
    return '';
  }

  if (typeof value === 'boolean') {
    return value ? 'true' : 'false';
  }

  if (typeof value === 'object') {
    return JSON.stringify(value);
  }

  return String(value);
}

function buildWorksheetXml(sheet: ExcelSheet): string {
  const columns = Array.from(
    new Set(sheet.rows.flatMap(row => Object.keys(row))),
  );

  const headerRow =
    columns.length > 0
      ? `<Row>${columns
          .map(
            column =>
              `<Cell ss:StyleID="header"><Data ss:Type="String">${escapeXml(
                column,
              )}</Data></Cell>`,
          )
          .join('')}</Row>`
      : '';

  const dataRows = sheet.rows
    .map(row => {
      const cells = columns
        .map(column => {
          const value = row[column];
          const stringValue = stringifyCellValue(value);
          return `<Cell><Data ss:Type="${getCellType(value)}">${escapeXml(
            stringValue,
          )}</Data></Cell>`;
        })
        .join('');

      return `<Row>${cells}</Row>`;
    })
    .join('');

  return `<Worksheet ss:Name="${escapeXml(
    sanitizeWorksheetName(sheet.name),
  )}"><Table>${headerRow}${dataRows}</Table></Worksheet>`;
}

function flattenSimulationHistory(simulationHistory: unknown[]) {
  return simulationHistory.map((item, index) => {
    const historyItem = item as {
      startTime?: string;
      endTime?: string;
      simulation?: {
        info?: { id?: string; documentName?: string; createdAt?: string; lastModifiedAt?: string };
        analytics?: {
          averageDuration?: number;
          averageWaiting?: number;
          averageTimeLoss?: number;
          totalNumberOfCarsThatCompleted?: number;
          simulationLength?: number;
        };
        statistics?: Record<string, unknown>;
        output?: { tripInfo?: unknown[]; netstate?: unknown[] };
      };
    };

    return {
      index: index + 1,
      startTime: historyItem.startTime,
      endTime: historyItem.endTime,
      simulationId: historyItem.simulation?.info?.id,
      documentName: historyItem.simulation?.info?.documentName,
      createdAt: historyItem.simulation?.info?.createdAt,
      lastModifiedAt: historyItem.simulation?.info?.lastModifiedAt,
      averageDuration: historyItem.simulation?.analytics?.averageDuration,
      averageWaiting: historyItem.simulation?.analytics?.averageWaiting,
      averageTimeLoss: historyItem.simulation?.analytics?.averageTimeLoss,
      completedCars:
        historyItem.simulation?.analytics?.totalNumberOfCarsThatCompleted,
      simulationLength: historyItem.simulation?.analytics?.simulationLength,
      tripInfoCount: historyItem.simulation?.output?.tripInfo?.length ?? 0,
      netstateCount: historyItem.simulation?.output?.netstate?.length ?? 0,
      statistics: JSON.stringify(historyItem.simulation?.statistics ?? {}),
    };
  });
}

function flattenTripInfoRows(simulationHistory: unknown[]) {
  return simulationHistory.flatMap((item, index) => {
    const historyItem = item as {
      startTime?: string;
      endTime?: string;
      simulation?: {
        info?: { id?: string; documentName?: string };
        output?: {
          tripInfo?: Array<{
            id: string;
            depart: number;
            departLane: string;
            departPos: number;
            departSpeed: number;
            departDelay: number;
            arrival: number;
            arrivalLane: string;
            arrivalPos: number;
            arrivalSpeed: number;
            duration: number;
            routeLength: number;
            waitingTime: number;
            waitingCount: number;
            stopTime: number;
            timeLoss: number;
            devices: string;
            vehicleType: string;
            speedFactor: number;
            vaporized: string | null;
          }>;
        };
      };
    };

    const simulationId = historyItem.simulation?.info?.id;
    const documentName = historyItem.simulation?.info?.documentName;

    return (historyItem.simulation?.output?.tripInfo ?? []).map(vehicle => ({
      simulationIndex: index + 1,
      simulationId,
      documentName,
      runStartTime: historyItem.startTime,
      runEndTime: historyItem.endTime,
      vehicleId: vehicle.id,
      vehicleType: vehicle.vehicleType,
      depart: vehicle.depart,
      departLane: vehicle.departLane,
      departPos: vehicle.departPos,
      departSpeed: vehicle.departSpeed,
      departDelay: vehicle.departDelay,
      arrival: vehicle.arrival,
      arrivalLane: vehicle.arrivalLane,
      arrivalPos: vehicle.arrivalPos,
      arrivalSpeed: vehicle.arrivalSpeed,
      duration: vehicle.duration,
      routeLength: vehicle.routeLength,
      waitingTime: vehicle.waitingTime,
      waitingCount: vehicle.waitingCount,
      stopTime: vehicle.stopTime,
      timeLoss: vehicle.timeLoss,
      speedFactor: vehicle.speedFactor,
      devices: vehicle.devices,
      vaporized: vehicle.vaporized,
    }));
  });
}

function flattenNetstateRows(simulationHistory: unknown[]) {
  return simulationHistory.flatMap((item, index) => {
    const historyItem = item as {
      simulation?: {
        info?: { id?: string; documentName?: string };
        output?: {
          netstate?: Array<{
            time: number;
            edges: Array<{
              id: string;
              lanes: Array<{
                id: string;
                vehicles: Array<{
                  id: string;
                  pos: number;
                  speed: number;
                }>;
              }>;
            }>;
          }>;
        };
      };
    };

    const simulationId = historyItem.simulation?.info?.id;
    const documentName = historyItem.simulation?.info?.documentName;

    return (historyItem.simulation?.output?.netstate ?? []).flatMap(timestep =>
      timestep.edges.flatMap(edge =>
        edge.lanes.flatMap(lane =>
          lane.vehicles.map(vehicle => ({
            simulationIndex: index + 1,
            simulationId,
            documentName,
            time: timestep.time,
            edgeId: edge.id,
            laneId: lane.id,
            vehicleId: vehicle.id,
            pos: vehicle.pos,
            speed: vehicle.speed,
          })),
        ),
      ),
    );
  });
}

function flattenStatisticsRows(simulationHistory: unknown[]) {
  return simulationHistory.map((item, index) => {
    const historyItem = item as {
      simulation?: {
        info?: { id?: string; documentName?: string };
        statistics?: {
          performance?: {
            clockBegin?: string;
            clockEnd?: string;
            clockDuration?: number;
            traciDuration?: number;
            realTimeFactor?: number;
            vehicleUpdatesPerSecond?: number;
            personUpdatesPerSecond?: number;
            begin?: number;
            end?: number;
            duration?: number;
          };
          vehicles?: {
            loaded?: number;
            inserted?: number;
            running?: number;
            waiting?: number;
          };
          teleports?: {
            total?: number;
            jam?: number;
            yield?: number;
            wrongLane?: number;
          };
          safety?: {
            collisions?: number;
            emergencyStops?: number;
            emergencyBraking?: number;
          };
          vehicleTripStatistics?: {
            count?: number;
            routeLength?: number;
            speed?: number;
            duration?: number;
            waitingTime?: number;
            timeLoss?: number;
            departDelay?: number;
            departDelayWaiting?: number;
            totalTravelTime?: number;
            totalDepartDelay?: number;
          };
        };
      };
    };

    return {
      simulationIndex: index + 1,
      simulationId: historyItem.simulation?.info?.id,
      documentName: historyItem.simulation?.info?.documentName,
      performanceClockBegin:
        historyItem.simulation?.statistics?.performance?.clockBegin,
      performanceClockEnd:
        historyItem.simulation?.statistics?.performance?.clockEnd,
      performanceClockDuration:
        historyItem.simulation?.statistics?.performance?.clockDuration,
      performanceTraciDuration:
        historyItem.simulation?.statistics?.performance?.traciDuration,
      performanceRealTimeFactor:
        historyItem.simulation?.statistics?.performance?.realTimeFactor,
      performanceVehicleUpdatesPerSecond:
        historyItem.simulation?.statistics?.performance?.vehicleUpdatesPerSecond,
      performancePersonUpdatesPerSecond:
        historyItem.simulation?.statistics?.performance?.personUpdatesPerSecond,
      performanceBegin:
        historyItem.simulation?.statistics?.performance?.begin,
      performanceEnd:
        historyItem.simulation?.statistics?.performance?.end,
      performanceDuration:
        historyItem.simulation?.statistics?.performance?.duration,
      vehiclesLoaded: historyItem.simulation?.statistics?.vehicles?.loaded,
      vehiclesInserted: historyItem.simulation?.statistics?.vehicles?.inserted,
      vehiclesRunning: historyItem.simulation?.statistics?.vehicles?.running,
      vehiclesWaiting: historyItem.simulation?.statistics?.vehicles?.waiting,
      teleportsTotal: historyItem.simulation?.statistics?.teleports?.total,
      teleportsJam: historyItem.simulation?.statistics?.teleports?.jam,
      teleportsYield: historyItem.simulation?.statistics?.teleports?.yield,
      teleportsWrongLane:
        historyItem.simulation?.statistics?.teleports?.wrongLane,
      safetyCollisions: historyItem.simulation?.statistics?.safety?.collisions,
      safetyEmergencyStops:
        historyItem.simulation?.statistics?.safety?.emergencyStops,
      safetyEmergencyBraking:
        historyItem.simulation?.statistics?.safety?.emergencyBraking,
      tripCount:
        historyItem.simulation?.statistics?.vehicleTripStatistics?.count,
      tripRouteLength:
        historyItem.simulation?.statistics?.vehicleTripStatistics?.routeLength,
      tripAverageSpeed:
        historyItem.simulation?.statistics?.vehicleTripStatistics?.speed,
      tripAverageDuration:
        historyItem.simulation?.statistics?.vehicleTripStatistics?.duration,
      tripWaitingTime:
        historyItem.simulation?.statistics?.vehicleTripStatistics?.waitingTime,
      tripTimeLoss:
        historyItem.simulation?.statistics?.vehicleTripStatistics?.timeLoss,
      tripDepartDelay:
        historyItem.simulation?.statistics?.vehicleTripStatistics?.departDelay,
      tripDepartDelayWaiting:
        historyItem.simulation?.statistics?.vehicleTripStatistics
          ?.departDelayWaiting,
      tripTotalTravelTime:
        historyItem.simulation?.statistics?.vehicleTripStatistics
          ?.totalTravelTime,
      tripTotalDepartDelay:
        historyItem.simulation?.statistics?.vehicleTripStatistics
          ?.totalDepartDelay,
    };
  });
}

export function buildUrbanfloExcelWorkbook(jsonString: string): string {
  const urbanfloFile = getNetworkFromUploadedFile(JSON.parse(jsonString));

  const sheets: ExcelSheet[] = [
    {
      name: 'Summary',
      rows: [
        {
          version: urbanfloFile.version,
          documentName: urbanfloFile.networkData.documentName,
          nodes: Object.keys(urbanfloFile.networkData.nodes).length,
          edges: Object.keys(urbanfloFile.networkData.edges).length,
          connections: Object.keys(urbanfloFile.networkData.connections).length,
          routes: Object.keys(urbanfloFile.networkData.route).length,
          flows: Object.keys(urbanfloFile.networkData.flow).length,
          vehicleTypes: Object.keys(urbanfloFile.networkData.vType).length,
          decorations: urbanfloFile.decorations.items.length,
          simulationRuns: urbanfloFile.simulationHistory.length,
        },
      ],
    },
    {
      name: 'Nodes',
      rows: Object.values(urbanfloFile.networkData.nodes),
    },
    {
      name: 'Edges',
      rows: Object.values(urbanfloFile.networkData.edges),
    },
    {
      name: 'Connections',
      rows: Object.entries(urbanfloFile.networkData.connections).map(
        ([id, connection]) => ({
          id,
          ...connection,
        }),
      ),
    },
    {
      name: 'Routes',
      rows: Object.values(urbanfloFile.networkData.route),
    },
    {
      name: 'Flows',
      rows: Object.values(urbanfloFile.networkData.flow),
    },
    {
      name: 'VehicleTypes',
      rows: Object.values(urbanfloFile.networkData.vType),
    },
    {
      name: 'Decorations',
      rows: urbanfloFile.decorations.items,
    },
    {
      name: 'SimulationHistory',
      rows: flattenSimulationHistory(urbanfloFile.simulationHistory),
    },
    {
      name: 'TripInfoDataset',
      rows: flattenTripInfoRows(urbanfloFile.simulationHistory),
    },
    {
      name: 'NetstateDataset',
      rows: flattenNetstateRows(urbanfloFile.simulationHistory),
    },
    {
      name: 'StatisticsDataset',
      rows: flattenStatisticsRows(urbanfloFile.simulationHistory),
    },
  ];

  const workbookXml = sheets.map(buildWorksheetXml).join('');

  return `<?xml version="1.0"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook
  xmlns="urn:schemas-microsoft-com:office:spreadsheet"
  xmlns:o="urn:schemas-microsoft-com:office:office"
  xmlns:x="urn:schemas-microsoft-com:office:excel"
  xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
  xmlns:html="http://www.w3.org/TR/REC-html40">
  <Styles>
    <Style ss:ID="header">
      <Font ss:Bold="1" />
    </Style>
  </Styles>
  ${workbookXml}
</Workbook>`;
}

export function geturbanfloExcelContents(): string {
  return buildUrbanfloExcelWorkbook(geturbanfloFileContents());
}
