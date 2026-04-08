import { Decoration, useDecorationStore } from '~/zustand/useDecorations';
import { NetworkData, useNetworkStore } from '~/zustand/useNetworkStore';
import {
  SimulationHistory,
  useSimulationHistory,
} from '~/zustand/useSimulationHistory';

/**
 * Defines the structure of an urbanflo file.
 */
interface urbanfloFile {
  version: number;
  networkData: NetworkData;
  simulationHistory: SimulationHistory[];
  decorations: {
    items: Decoration[];
  };
}

/**
 * Returns the content of an urbanflo file as a string.
 *
 * @returns {string} - String representation of the urbanflo file.
 */
export function geturbanfloFileContents(): string {
  const fileContents = getFileContents();

  const jsonString = JSON.stringify(fileContents, null, 2);

  return jsonString;
}

/**
 * Retrieves the contents required for an urbanflo file.
 *
 * @returns {urbanfloFile} - The contents of the urbanflo file.
 */
function getFileContents(): urbanfloFile {
  const networkStore = useNetworkStore.getState();
  const simulationHistoryStore = useSimulationHistory.getState();
  const decorationStore = useDecorationStore.getState();

  return {
    version: 1,
    networkData: {
      documentName: networkStore.documentName,
      nodes: networkStore.nodes,
      edges: networkStore.edges,
      connections: networkStore.connections,
      vType: networkStore.vType,
      route: networkStore.route,
      flow: networkStore.flow,
    },
    simulationHistory: simulationHistoryStore.history,
    decorations: {
      items: decorationStore.items,
    },
  };
}

/**
 * Converts a JSON object to an urbanfloFile.
 *
 * @param {unknown} json - The JSON object to be converted.
 * @returns {urbanfloFile} - The urbanfloFile representation of the provided JSON.
 */
export function getNetworkFromUploadedFile(json: unknown): urbanfloFile {
  return json as urbanfloFile;
}
