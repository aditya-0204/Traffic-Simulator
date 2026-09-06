import { useMemo } from 'react';
import { Circle, Layer, Text } from 'react-konva';

import { getCongestionZones } from '~/helpers/congestion';
import { useCarsStore } from '~/zustand/useCarStore';
import { useNetworkStore } from '~/zustand/useNetworkStore';

const DETECTION_RADIUS = 55;
const MINIMUM_CARS_FOR_CONGESTION = 4;

export function CongestionLayer() {
  const nodes = useNetworkStore(state => Object.values(state.nodes));
  const cars = useCarsStore(state => state.cars);

  const congestionZones = useMemo(
    () =>
      getCongestionZones(
        nodes,
        cars,
        {
          detectionRadius: DETECTION_RADIUS,
          minimumCars: MINIMUM_CARS_FOR_CONGESTION,
        },
      ),
    [cars, nodes],
  );

  return (
    <Layer listening={false}>
      {congestionZones.map(zone => {
        const label = `${zone.count}`;

        return (
          <>
            <Circle
              key={`${zone.nodeId}-circle`}
              x={zone.x}
              y={zone.y}
              radius={zone.radius}
              fill={zone.fill}
              opacity={zone.opacity}
            />
            <Text
              key={`${zone.nodeId}-label`}
              x={zone.x - zone.radius / 2}
              y={zone.y - 8}
              width={zone.radius}
              align="center"
              text={label}
              fontStyle="bold"
              fontSize={16}
              fill="#7f1d1d"
            />
          </>
        );
      })}
    </Layer>
  );
}
