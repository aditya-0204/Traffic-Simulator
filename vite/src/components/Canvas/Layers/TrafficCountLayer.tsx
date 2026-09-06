import { Layer, Text } from 'react-konva';

import { useCarsStore } from '~/zustand/useCarStore';

export function TrafficCountLayer() {
  const carCount = useCarsStore(state => state.cars.length);

  return (
    <Layer listening={false}>
      <Text
        x={18}
        y={18}
        // text={`Traffic: ${carCount}`}
        fontSize={18}
        fontStyle="bold"
        fill="#1f2937"
        padding={10}
        cornerRadius={12}
        background="#ffffff"
        shadowColor="#000000"
        shadowBlur={6}
        shadowOpacity={0.15}
      />
    </Layer>
  );
}
