import { Circle, Group } from 'react-konva';

import { Car as CarType } from '~/zustand/useCarStore';

interface CarProps {
  car: CarType;
}

export function Car({ car }: CarProps) {
  return (
    <Group x={car.location.x} y={car.location.y} listening={false}>
      <Circle radius={3.4} fill="#ffffff" opacity={0.9} />
      <Circle radius={2.4} fill={car.color} />
    </Group>
  );
}
