import { geturbanfloExcelContents } from '~/logic/urbanflo-excel-logic';

function ensureExcelExtension(fileName: string) {
  const trimmed = fileName.trim();

  if (trimmed.length === 0) {
    return 'urbanflo-export.xls';
  }

  return trimmed.toLowerCase().endsWith('.xls')
    ? trimmed
    : trimmed.replace(/\.json$/i, '') + '.xls';
}

const EXCEL_MIME_TYPE = 'application/vnd.ms-excel';

const useExcelDownloader = () => {
  const downloadExcel = (fileName: string) => {
    const workbook = geturbanfloExcelContents();
    const blob = new Blob([workbook], { type: EXCEL_MIME_TYPE });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    link.href = url;
    link.download = ensureExcelExtension(fileName);
    document.body.appendChild(link);

    link.click();

    link.remove();
    URL.revokeObjectURL(url);
  };

  return downloadExcel;
};

export default useExcelDownloader;
