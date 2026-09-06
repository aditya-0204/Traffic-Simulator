import { getCongestionZones } from './index';

describe('getCongestionZones', () => {
  it('creates a congestion zone when enough cars cluster near a node', () => {
    const zones = getCongestionZones(
      [
        { id: 'A', x: 100, y: 100, type: 'priority' },
        { id: 'B', x: 300, y: 300, type: 'priority' },
      ],
      [
        { location: { x: 110, y: 90 }, color: '#ef4444', speed: 0.2 },
        { location: { x: 120, y: 100 }, color: '#ef4444', speed: 0.5 },
        { location: { x: 95, y: 130 }, color: '#ef4444', speed: 1.5 },
        { location: { x: 80, y: 95 }, color: '#ef4444', speed: 4.5 },
        { location: { x: 305, y: 295 }, color: '#ef4444', speed: 0.1 },
      ],
      { detectionRadius: 45, minimumCars: 4 },
    );

    expect(zones).toHaveLength(1);
    expect(zones[0]).toEqual(
      expect.objectContaining({
        nodeId: 'A',
        count: 4,
        score: 9,
        fill: '#f97316',
        label: 'Medium',
        x: 100,
        y: 100,
      }),
    );
  });

  it('does not create a congestion zone when the nearby car count is too low', () => {
    const zones = getCongestionZones(
      [{ id: 'A', x: 100, y: 100, type: 'priority' }],
      [
        { location: { x: 110, y: 90 }, color: '#ef4444', speed: 0.2 },
        { location: { x: 120, y: 100 }, color: '#ef4444', speed: 0.5 },
        { location: { x: 95, y: 130 }, color: '#ef4444', speed: 1.5 },
      ],
      { detectionRadius: 45, minimumCars: 4 },
    );

    expect(zones).toEqual([]);
  });
});
