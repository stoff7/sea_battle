import { createShip } from './shipFactory';

export class FieldFacade {
  constructor(gridSize = 10) {
    this.gridSize = gridSize;
  }

  // Проверка, можно ли разместить корабль
  canPlaceAt(ships, cellIndices, ignoreShipId = null) {
    const forbidden = new Set();
    for (const ship of ships) {
      if (ship.id === ignoreShipId) continue;
      ship.coords.forEach(([x, y]) => forbidden.add(y * this.gridSize + x));
      this.getAdjacentCells(ship).forEach(i => forbidden.add(i));
    }
    return cellIndices.every(i => !forbidden.has(i));
  }

  // Получить соседние клетки корабля
  getAdjacentCells(ship) {
    const deltas = [
      [-1, -1], [-1, 0], [-1, 1],
      [0, -1], [0, 1],
      [1, -1], [1, 0], [1, 1],
    ];
    const neighbors = new Set();
    for (const [x, y] of ship.coords) {
      for (const [dx, dy] of deltas) {
        const nx = x + dx;
        const ny = y + dy;
        if (
          nx >= 0 && nx < this.gridSize &&
          ny >= 0 && ny < this.gridSize &&
          !ship.coords.some(([sx, sy]) => sx === nx && sy === ny)
        ) {
          neighbors.add(ny * this.gridSize + nx);
        }
      }
    }
    return neighbors;
  }

  // Вращение корабля (возвращает новый объект)
  rotateShip(ship, grabbedIndex = 0) {
    const length = ship.coords.length;
    const [cx, cy] = ship.coords[grabbedIndex];
    const newDirection = ship.direction === 'horizontal' ? 'vertical' : 'horizontal';
    let newCoords = [];
    if (newDirection === 'horizontal') {
      for (let i = 0; i < length; i++) newCoords.push([cx - grabbedIndex + i, cy]);
      newCoords.sort((a, b) => a[0] - b[0]);
    } else {
      for (let i = 0; i < length; i++) newCoords.push([cx, cy - grabbedIndex + i]);
      newCoords.sort((a, b) => a[1] - b[1]);
    }
    return createShip({
      ...ship,
      direction: newDirection,
      w: newDirection === 'horizontal' ? length : 1,
      h: newDirection === 'vertical' ? length : 1,
      coords: newCoords,
    });
  }
}

// Singleton
let fieldFacadeInstance = null;
export function getFieldFacade(gridSize = 10) {
  if (!fieldFacadeInstance) {
    fieldFacadeInstance = new FieldFacade(gridSize);
  }
  return fieldFacadeInstance;
}