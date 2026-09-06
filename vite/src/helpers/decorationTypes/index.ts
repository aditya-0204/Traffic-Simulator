import { LabelNamesType } from '~/types/Toolbar';
import { DecorationTypeNames } from '~/zustand/useDecorations';

export function toolbarItemToDecoration(
  item: LabelNamesType,
): DecorationTypeNames {
  switch (item) {
    case 'Tree':
      return 'tree';
    case 'Building':
      return 'building';
    case 'Flyover':
      return 'flyover';
    case 'Roundabout':
      return 'roundabout';
    default:
      throw new Error(`${item} is an invalid decoration type`);
  }
}
