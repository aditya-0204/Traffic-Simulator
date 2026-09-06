import { extractCarsFromSumoMessage } from '.';

describe('extractCarsFromSumoMessage', () => {
  it('should throw error for invalid JSON', () => {
    expect(() => {
      extractCarsFromSumoMessage('invalid JSON');
    }).toThrowError('Invalid JSON format');
  });

  it('should throw error for invalid car format (missing position)', () => {
    const sumoMessage = JSON.stringify({
      'vehicle-1': { vehicleId: 'vehicle-1', position: {} },
    });
    expect(() => {
      extractCarsFromSumoMessage(sumoMessage);
    }).toThrowError('Response is not in car format');
  });

  it('should return an empty array for an empty input', () => {
    const sumoMessage = JSON.stringify({});
    const cars = extractCarsFromSumoMessage(sumoMessage);
    expect(cars).toEqual([]);
  });

  it('should return an array of cars for valid input', () => {
    const sumoMessage = JSON.stringify({
      'vehicle-1': {
        vehicleId: 'vehicle-1',
        position: { first: 1, second: 2 },
        color: '#ef4444',
      },
      'vehicle-2': {
        vehicleId: 'vehicle-2',
        position: { first: 3, second: 4 },
        color: '#ef4444',
      },
    });
    const cars = extractCarsFromSumoMessage(sumoMessage);
    expect(cars).toEqual([
      { location: { x: 1, y: 2 }, color: '#ef4444', speed: 0 },
      { location: { x: 3, y: 4 }, color: '#ef4444', speed: 0 },
    ]);
  });

  it('should accept zero coordinates as valid positions', () => {
    const sumoMessage = JSON.stringify({
      'vehicle-1': {
        vehicleId: 'vehicle-1',
        position: { first: 0, second: 5 },
        color: '#ef4444',
        speed: 12,
      },
    });

    const cars = extractCarsFromSumoMessage(sumoMessage);
    expect(cars).toEqual([
      { location: { x: 0, y: 5 }, color: '#ef4444', speed: 12 },
    ]);
  });

  it('should map ambulance vehicles to green when no color is supplied', () => {
    const sumoMessage = JSON.stringify({
      'ambulance-1': {
        vehicleId: 'ambulance-1',
        position: { first: 1, second: 2 },
      },
    });

    const cars = extractCarsFromSumoMessage(sumoMessage);
    expect(cars).toEqual([
      { location: { x: 1, y: 2 }, color: '#16a34a', speed: 0 },
    ]);
  });

  it('should map fire brigade vehicles to blue when no color is supplied', () => {
    const sumoMessage = JSON.stringify({
      'firebrigade-1': {
        vehicleId: 'firebrigade-1',
        position: { first: 3, second: 4 },
      },
    });

    const cars = extractCarsFromSumoMessage(sumoMessage);
    expect(cars).toEqual([
      { location: { x: 3, y: 4 }, color: '#2563eb', speed: 0 },
    ]);
  });

  it('should preserve vehicle speed when supplied', () => {
    const sumoMessage = JSON.stringify({
      'vehicle-1': {
        vehicleId: 'vehicle-1',
        position: { first: 1, second: 2 },
        speed: 3.5,
      },
    });

    const cars = extractCarsFromSumoMessage(sumoMessage);
    expect(cars).toEqual([
      { location: { x: 1, y: 2 }, color: '#ef4444', speed: 3.5 },
    ]);
  });
});
