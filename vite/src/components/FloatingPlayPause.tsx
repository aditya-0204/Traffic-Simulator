import { useEffect, useRef, useState } from 'react';
import { CircleLoader } from 'react-spinners';

import {
  ExclamationTriangleIcon,
  PlayIcon,
  StopIcon,
} from '@heroicons/react/24/outline';

import {
  getSimulationAnalytics,
  getSimulationOutput,
  getSimulationOutputStatistics,
  uploadNetwork,
} from '~/api/network';
import { extractCarsFromSumoMessage } from '~/helpers/sumo';
import { networkHasData } from '~/helpers/zustand/NetworkStoreHelpers';
import { useSimulation } from '~/hooks/useSimulation';
import { buildSimulationPayload } from '~/logic/simulation-payload';
import {
  BASE_SIMULATION_DATA_TOPIC,
  BASE_SIMULATION_DESTINATION_PATH,
  BASE_SIMULATION_ERROR_TOPIC,
  SIMULATION_SOCKET_URL,
} from '~/simulation-urls';
import { SimulationInfo } from '~/types/Simulation';
import { useCarsStore } from '~/zustand/useCarStore';
import { useErrorModal } from '~/zustand/useErrorModal.ts';
import { useNetworkStore } from '~/zustand/useNetworkStore';
import { usePlaying } from '~/zustand/usePlaying';
import { useSimulationHistory } from '~/zustand/useSimulationHistory';

const OUTPUT_RETRY_DELAY_MS = 1500;
const OUTPUT_RETRY_ATTEMPTS = 10;
const STOP_SETTLE_DELAY_MS = 2500;

function sleep(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function fetchSimulationArtifactsWithRetry(simulationId: string) {
  let lastError: unknown = null;

  for (let attempt = 1; attempt <= OUTPUT_RETRY_ATTEMPTS; attempt += 1) {
    try {
      const [simOutput, simOutputStatistics, simAnalytics] = await Promise.all([
        getSimulationOutput(simulationId),
        getSimulationOutputStatistics(simulationId),
        getSimulationAnalytics(simulationId),
      ]);

      return {
        simOutput,
        simOutputStatistics,
        simAnalytics,
      };
    } catch (error) {
      lastError = error;

      if (attempt < OUTPUT_RETRY_ATTEMPTS) {
        await sleep(OUTPUT_RETRY_DELAY_MS);
      }
    }
  }

  throw lastError ?? new Error('Unable to get simulation output');
}

export const FloatingPlayPause = () => {
  const [loading, setLoading] = useState(false);
  const network = useNetworkStore();
  const setCars = useCarsStore(state => state.setCars);
  const player = usePlaying();
  const { subscribe, unsubscribe, publish, isConnected, error } = useSimulation({
    brokerURL: SIMULATION_SOCKET_URL,
  });

  const simulationHistory = useSimulationHistory();
  const errorModal = useErrorModal();

  const [startTime, setStartTime] = useState<string | null>(null);
  const [simulationInfo, setSimulationInfo] = useState<SimulationInfo | null>(
    null,
  );
  const activeSimulationIdRef = useRef<string | null>(null);

  // streaming of simulation data
  useEffect(() => {
    if (!player.simulationId) {
      return;
    }

    const SIMULATION_DATA_TOPIC = `${BASE_SIMULATION_DATA_TOPIC}/${player.simulationId}`;
    const SIMULATION_ERROR_TOPIC = BASE_SIMULATION_ERROR_TOPIC.replace(
      '_',
      player.simulationId ?? '',
    );
    const SIMULATION_DESTINATION_PATH = `${BASE_SIMULATION_DESTINATION_PATH}/${player.simulationId}`;

    if (player.isPlaying && isConnected) {
      if (activeSimulationIdRef.current === player.simulationId) {
        return;
      }

      console.warn('Subscribing to simulation');

      subscribe(SIMULATION_DATA_TOPIC, message => {
        const data = extractCarsFromSumoMessage(message);

        if (data) {
          setCars(data);
        }
      });
      subscribe(SIMULATION_ERROR_TOPIC, message => {
        console.error(message);
        errorModal.open(
          'An error occurred while simulation is running',
          message,
        );
      });

      publish(SIMULATION_DESTINATION_PATH, { status: 'START' });
      activeSimulationIdRef.current = player.simulationId;
    } else if (!player.isPlaying && isConnected && player.simulationId) {
      console.warn('Unsubscribing from simulation');
      publish(SIMULATION_DESTINATION_PATH, { status: 'STOP' });
      unsubscribe(SIMULATION_DATA_TOPIC);
      unsubscribe(SIMULATION_ERROR_TOPIC);
      activeSimulationIdRef.current = null;
    }
  }, [errorModal, isConnected, player.isPlaying, player.simulationId, publish, setCars, subscribe, unsubscribe]);

  const handleUpload = async () => {
    try {
      setLoading(true);
      const requestBody = buildSimulationPayload(network);

      const simInfo = await uploadNetwork(requestBody);
      setStartTime(new Date().toISOString());
      setSimulationInfo(simInfo);
      setCars([]);
      player.changeSimulationId(simInfo.id);
      player.play();
    } catch (error) {
      console.error(error);
      errorModal.open('Unable to start simulation', (error as Error).message);
    } finally {
      setLoading(false);
    }
  };

  const handleOutput = async () => {
    const currentSimulationId = player.simulationId;

    try {
      setLoading(true);
      player.pause();

      // Give the backend a moment to process STOP and flush output files.
      await sleep(STOP_SETTLE_DELAY_MS);

      if (!currentSimulationId) {
        return;
      }

      const {
        simOutput,
        simOutputStatistics,
        simAnalytics,
      } = await fetchSimulationArtifactsWithRetry(currentSimulationId);

      if (startTime && simulationInfo) {
        simulationHistory.updateHistory({
          startTime,
          endTime: new Date().toISOString(),
          simulation: {
            info: simulationInfo,
            output: simOutput,
            statistics: simOutputStatistics,
            analytics: simAnalytics,
          },
        });
      }
    } catch (error: unknown) {
      console.error(error);
      errorModal.open(
        'Unable to get simulation output',
        (error as Error).message,
      );
    } finally {
      player.changeSimulationId(null);
      setSimulationInfo(null);
      setStartTime(null);
      activeSimulationIdRef.current = null;
      setLoading(false);
    }
  };

  const buttonDisabled = loading || !!error || !networkHasData(network);

  return (
    <div className="absolute bottom-4 right-4 py-2 items-center justify-center rounded-full flex z-10 gap-4">
      {error && (
        <div className="flex items-center bg-red-100 p-2 rounded-full shadow-lg animate-fadeIn transform-gpu">
          <ExclamationTriangleIcon
            width={24}
            height={24}
            className="text-red-500 animate-bounce"
          />
          <span className="text-red-500 font-bold ml-2">{error.message}</span>
        </div>
      )}
      <button
        onClick={player.isPlaying ? handleOutput : handleUpload}
        className={`flex items-center bg-orange-500 text-white font-sans w-24 rounded-full font-bold h-10 px-4 py-2 ${
          loading ? 'justify-center' : 'justify-between'
        } disabled:cursor-not-allowed disabled:text-gray-700 disabled:bg-gray-300`}
        disabled={buttonDisabled}
      >
        {loading ? (
          <CircleLoader size={20} />
        ) : player.isPlaying ? (
          'End'
        ) : (
          'Start'
        )}
        {loading ? null : player.isPlaying ? (
          <StopIcon className="h-5 ml-2" strokeWidth={3} />
        ) : (
          <PlayIcon className="h-5 ml-2" strokeWidth={3} />
        )}
      </button>
    </div>
  );
};
