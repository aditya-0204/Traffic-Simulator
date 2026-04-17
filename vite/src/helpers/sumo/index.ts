import { Car } from '~/zustand/useCarStore';

export type SumoMessage = {
  [vehicleId: string]: {
    vehicleId: string;
    position: {
      first: number;
      second: number;
    };
    color: string;
    acceleration: number;
    speed: number;
    lane: {
      first: number;
      second: string;
    };
  };
};

function getVehicleColor(vehicleId: string, color?: string) {
  if (color && color.trim().length > 0) {
    return color;
  }

  const normalizedId = vehicleId.toLowerCase();

  if (normalizedId.includes('firebrigade') || normalizedId.includes('fire_brigade') || normalizedId.includes('fire-brigade') || normalizedId.includes('fire')) {
    return '#2563eb';
  }

  if (normalizedId.includes('ambulance')) {
    return '#16a34a';
  }

  return '#ef4444';
}

/**
 * Extracts and transforms cars from a SUMO message to match the required Car format.
 * @param {string} sumoMessage - The SUMO message as a JSON string.
 * @returns {Car[]} An array of cars transformed from the SUMO message.
 * @throws {Error} Throws an error if the SUMO message is in an invalid format or cannot be parsed.
 */
export function extractCarsFromSumoMessage(sumoMessage: string): Car[] {
  let parsedJson: SumoMessage;
  const cars: Car[] = [];

  try {
    parsedJson = JSON.parse(sumoMessage);
  } catch (err) {
    throw new Error('Invalid JSON format');
  }

  for (const vehicleId in parsedJson) {
    const vehicle = parsedJson[vehicleId];
    if (
      !vehicle.position ||
      !vehicle.position.first ||
      !vehicle.position.second
    ) {
      throw new Error('Response is not in car format');
    }

    cars.push({
      location: {
        x: vehicle.position.first,
        y: vehicle.position.second,
      },
      color: getVehicleColor(vehicle.vehicleId ?? vehicleId, vehicle.color),
    });
  }

  return cars;
}
