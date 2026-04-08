import { canvasComponentBg } from '~/colors';
import { useToolbarStore } from '~/zustand/useToolbar';

import { ToolbarDivider } from './ToolbarDivider';
import { ToolBarItem } from './ToolbarItem';

export function Toolbar() {
  const toolbarStore = useToolbarStore();

  return (
    <span
      style={{
        transform: '',
        backgroundColor: canvasComponentBg,
      }}
      className={`fixed ${
        toolbarStore.isOpen ? 'flex flex-col' : 'hidden'
      } top-64 right-10 shadow-md p-2 rounded-xl`}
    >
      {toolbarStore.items.map((item, idx) => {
        if (item.divider) {
          return <ToolbarDivider key={idx} />;
        }
        return <ToolBarItem toolbarItem={item} key={idx} />;
      })}
    </span>
  );
}
