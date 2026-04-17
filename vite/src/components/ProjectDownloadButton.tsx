import { ArrowDownTrayIcon, TableCellsIcon } from '@heroicons/react/20/solid';

import { handleDownloadEvent } from '~/helpers/zustand/NetworkStoreHelpers.ts';
import useExcelDownloader from '~/hooks/useExcelDownloader';
import useJsonDownloader from '~/hooks/useJsonDownloader';
import { useNetworkStore } from '~/zustand/useNetworkStore';

export function ProjectDownloadButton() {
  const downloadJson = useJsonDownloader();
  const downloadExcel = useExcelDownloader();
  const network = useNetworkStore();

  function handleDownloadClick() {
    handleDownloadEvent(downloadJson, network);
  }

  function handleExcelDownloadClick() {
    downloadExcel(network.documentName);
  }

  return (
    <div className="flex gap-3">
      <button
        className="text-sm font-semibold bg-green-500 leading-6 items-center rounded-full flex py-2 px-4 mt-4"
        onClick={handleDownloadClick}
        title="Download JSON"
      >
        <ArrowDownTrayIcon className="h-6 w-6 ml-2" aria-hidden="true" />
      </button>
      <button
        className="text-sm font-semibold bg-emerald-700 text-white leading-6 items-center rounded-full flex py-2 px-4 mt-4"
        onClick={handleExcelDownloadClick}
        title="Download Excel"
      >
        <TableCellsIcon className="h-6 w-6 ml-2" aria-hidden="true" />
      </button>
    </div>
  );
}
