import { Car } from '~/zustand/useCarStore';
import { Node } from '~/types/Network';

export type CongestionZone = {
  nodeId: string;
  count: number;
  score: number;
  radius: number;
  opacity: number;
  fill: string;
  label: string;
  x: number;
  y: number;
};

type CongestionOptions = {
  detectionRadius?: number;
  minimumCars?: number;
};

const DEFAULT_DETECTION_RADIUS = 55;
const DEFAULT_MINIMUM_CARS = 4;
const MAX_ZONE_RADIUS = 90;
const SLOW_SPEED_THRESHOLD = 3;
const STOPPED_SPEED_THRESHOLD = 0.75;

function getSeverity(score: number) {
  if (score >= 10) {
    return { fill: '#dc2626', label: 'High', opacity: 0.38 };
  }

  if (score >= 7) {
    return { fill: '#f97316', label: 'Medium', opacity: 0.3 };
  }

  return { fill: '#facc15', label: 'Low', opacity: 0.24 };
}

export function getCongestionZones(
  nodes: Node[],
  cars: Car[],
  options: CongestionOptions = {},
): CongestionZone[] {
  const detectionRadius =
    options.detectionRadius ?? DEFAULT_DETECTION_RADIUS;
  const minimumCars = options.minimumCars ?? DEFAULT_MINIMUM_CARS;
  const maxDistanceSquared = detectionRadius * detectionRadius;

  return nodes.flatMap(node => {
    let count = 0;
    let score = 0;

    for (const car of cars) {
      const dx = car.location.x - node.x;
      const dy = car.location.y - node.y;
      const distanceSquared = dx * dx + dy * dy;

      if (distanceSquared <= maxDistanceSquared) {
        count += 1;
        score += 1;

        if (car.speed <= STOPPED_SPEED_THRESHOLD) {
          score += 2;
        } else if (car.speed <= SLOW_SPEED_THRESHOLD) {
          score += 1;
        }
      }
    }

    if (count < minimumCars) {
      return [];
    }

    const extraCars = count - minimumCars;
    const severity = getSeverity(score);
    const radius = Math.min(
      detectionRadius + extraCars * 4,
      MAX_ZONE_RADIUS,
    );
    const opacity = Math.min(severity.opacity + extraCars * 0.02, 0.46);

    return [
      {
        nodeId: node.id,
        count,
        score,
        radius,
        opacity,
        fill: severity.fill,
        label: severity.label,
        x: node.x,
        y: node.y,
      },
    ];
  });
}
