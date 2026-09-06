import React from 'react';
import { Group, Line, Rect } from 'react-konva';

import { Decoration } from '~/zustand/useDecorations';

interface FlyoverProps {
  flyover: Decoration;
}

function FlyoverComponent({ flyover }: FlyoverProps) {
  const { x, y } = flyover;

  return (
    <Group>
      <Line
        points={[x - 36, y + 22, x + 36, y - 22]}
        stroke="#6b7280"
        strokeWidth={20}
        lineCap="round"
      />
      <Line
        points={[x - 36, y + 22, x + 36, y - 22]}
        stroke="#f9fafb"
        strokeWidth={2}
        dash={[8, 8]}
        lineCap="round"
      />
      <Rect
        x={x - 8}
        y={y + 8}
        width={10}
        height={24}
        fill="#9ca3af"
        cornerRadius={3}
        rotation={58}
      />
      <Rect
        x={x - 4}
        y={y - 22}
        width={10}
        height={24}
        fill="#9ca3af"
        cornerRadius={3}
        rotation={58}
      />
    </Group>
  );
}

export const Flyover = React.memo(FlyoverComponent);
