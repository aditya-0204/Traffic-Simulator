/**
 * @jest-environment jsdom
 */
import { renderHook } from '@testing-library/react';

import useExcelDownloader from '../useExcelDownloader';

describe('useExcelDownloader', () => {
  beforeAll(() => {
    global.URL.createObjectURL = jest.fn();
    global.URL.revokeObjectURL = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('downloads an excel file with an xls extension', () => {
    const { result } = renderHook(() => useExcelDownloader());

    const appendChildSpy = jest.spyOn(document.body, 'appendChild');
    const clickSpy = jest
      .spyOn(HTMLAnchorElement.prototype, 'click')
      .mockImplementation();
    const removeSpy = jest
      .spyOn(HTMLAnchorElement.prototype, 'remove')
      .mockImplementation();

    result.current('traffic-network.json');

    expect(global.URL.createObjectURL).toHaveBeenCalled();

    const addedAnchor = appendChildSpy.mock.calls[0][0] as HTMLAnchorElement;
    expect(addedAnchor.download).toBe('traffic-network.xls');
    expect(clickSpy).toHaveBeenCalled();
    expect(removeSpy).toHaveBeenCalled();
    expect(global.URL.revokeObjectURL).toHaveBeenCalled();
  });

  it('creates an excel blob', () => {
    const { result } = renderHook(() => useExcelDownloader());

    result.current('traffic-network');

    const blob = (global.URL.createObjectURL as jest.Mock).mock.calls[0][0];
    expect(blob).toBeInstanceOf(Blob);
    expect(blob.type).toBe('application/vnd.ms-excel');
  });

  it('falls back to a default filename when none is provided', () => {
    const { result } = renderHook(() => useExcelDownloader());

    const createElementSpy = jest.spyOn(document, 'createElement');

    result.current('');

    const addedAnchor = createElementSpy.mock.results[0]
      .value as HTMLAnchorElement;
    expect(addedAnchor.download).toBe('urbanflo-export.xls');

    createElementSpy.mockRestore();
  });
});
