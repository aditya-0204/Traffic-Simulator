import { Layer } from 'react-konva';

import { Decoration, useDecorationStore } from '~/zustand/useDecorations';

import { Building } from '../Decorations/Building';
import { Flyover } from '../Decorations/Flyover';
import { Roundabout } from '../Decorations/Roundabout';
import { Tree } from '../Decorations/Tree';

function isTree(item: Decoration) {
  return item.type === 'tree';
}

function isBuilding(item: Decoration) {
  return item.type === 'building';
}

function isFlyover(item: Decoration) {
  return item.type === 'flyover';
}

function isRoundabout(item: Decoration) {
  return item.type === 'roundabout';
}

export function DecorationsLayer() {
  const decorationsStore = useDecorationStore();

  const trees = decorationsStore.items.filter(isTree);
  const buildings = decorationsStore.items.filter(isBuilding);
  const flyovers = decorationsStore.items.filter(isFlyover);
  const roundabouts = decorationsStore.items.filter(isRoundabout);
  return (
    <Layer>
      {trees.map(item => {
        return <Tree tree={item} key={item.id} />;
      })}

      {buildings.map(item => {
        return <Building building={item} key={item.id} />;
      })}

      {flyovers.map(item => {
        return <Flyover flyover={item} key={item.id} />;
      })}

      {roundabouts.map(item => {
        return <Roundabout roundabout={item} key={item.id} />;
      })}
    </Layer>
  );
}
