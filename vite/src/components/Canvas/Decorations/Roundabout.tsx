import React from 'react';
import { Circle, Group, Line } from 'react-konva';

import { Decoration } from '~/zustand/useDecorations';

interface RoundaboutProps {
  roundabout: Decoration;
}

function RoundaboutComponent({ roundabout }: RoundaboutProps) {
  const { x, y } = roundabout;

  return (
    <Group>
      <Line
        points={[x - 52, y, x - 30, y]}
        stroke="#6b7280"
        strokeWidth={18}
        lineCap="round"
      />
      <Line
        points={[x + 30, y, x + 52, y]}
        stroke="#6b7280"
        strokeWidth={18}
        lineCap="round"
      />
      <Line
        points={[x, y - 52, x, y - 30]}
        stroke="#6b7280"
        strokeWidth={18}
        lineCap="round"
      />
      <Line
        points={[x, y + 30, x, y + 52]}
        stroke="#6b7280"
        strokeWidth={18}
        lineCap="round"
      />
      <Circle x={x} y={y} radius={28} fill="#6b7280" />
      <Circle x={x} y={y} radius={17} fill="#f3f4f6" />
      <Circle x={x} y={y} radius={6} fill="#84cc16" opacity={0.85} />
    </Group>
  );
}

export const Roundabout = React.memo(RoundaboutComponent);
