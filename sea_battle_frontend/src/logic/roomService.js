import { getApiFacade } from './apiFacade';
import { createShip } from './shipFactory';

class RoomService {
  constructor(api, gameId, playerId) {
    this.api = api;
    this.gameId = gameId;
    this.playerId = playerId;
    this.apiFacade = getApiFacade(`https://${api}/api/v1`);
  }

  async setReady(ready, ships) {
    // ships должен быть в формате для API
    return this.apiFacade.setReady(this.api, this.gameId, this.playerId, ready, ships);
  }

  async leaveRoom() {
    return this.apiFacade.leaveGame(this.api, this.gameId, this.playerId);
  }

  async sendChatMessage(text) {
    return this.apiFacade.sendChatMessage(this.api, this.gameId, this.playerId, text);
  }

  convertShipsForApi(ships) {
    const typeByLength = {
      4: 'FOUR_DECK',
      3: 'THREE_DECK',
      2: 'TWO_DECK',
      1: 'ONE_DECK'
    };
    return ships.map(ship => ({
      type: typeByLength[Math.max(ship.w, ship.h)],
      cells: ship.coords.map(([x, y]) => ({ x, y }))
    }));
  }

  randomizeShips(gridSize, availableShips) {
    // Возвращает массив кораблей с координатами
    const directions = [
      [1, 0], [-1, 0], [0, 1], [0, -1],
      [1, 1], [-1, -1], [1, -1], [-1, 1]
    ];
    const ships = [];
    const takenCoords = new Set();
    let shipsPool = [...availableShips];

    for (let o = 0; o < 150 && shipsPool.length > 0; o++) {
      let done = true;
      const randomX = Math.floor(Math.random() * gridSize);
      const randomY = Math.floor(Math.random() * gridSize);
      const randomShip = shipsPool[Math.floor(Math.random() * shipsPool.length)];
      let w = randomShip.w;
      let h = randomShip.h;
      const direction = Math.random() > 0.5 ? 'horizontal' : 'vertical';
      let newCoords = new Set();

      if (direction === 'horizontal') {
        for (let j = 0; j < randomShip.w; j++) {
          const key = [randomX + j, randomY].toString();
          if (takenCoords.has(key) || randomX + j >= gridSize || randomY >= gridSize) {
            done = false;
            break;
          }
          newCoords.add([randomX + j, randomY]);
        }
      } else {
        [w, h] = [h, w];
        for (let j = 0; j < randomShip.w; j++) {
          const key = [randomX, randomY + j].toString();
          if (takenCoords.has(key) || randomX >= gridSize || randomY + j >= gridSize) {
            done = false;
            break;
          }
          newCoords.add([randomX, randomY + j]);
        }
      }

      if (done) {
        newCoords.forEach(([x, y]) => {
          takenCoords.add([x, y].toString());
          directions.forEach(([dx, dy]) => {
            takenCoords.add([x + dx, y + dy].toString());
          });
        });
        ships.push(createShip({
          ...randomShip,
          direction,
          w,
          h,
          coords: Array.from(newCoords)
        }));
        shipsPool = shipsPool.filter(s => s.id !== randomShip.id);
      }
    }
    return ships;
  }
}

// Singleton для каждой комнаты
const roomServiceInstances = {};
export function getRoomService(api, gameId, playerId) {
  const key = `${api}_${gameId}_${playerId}`;
  if (!roomServiceInstances[key]) {
    roomServiceInstances[key] = new RoomService(api, gameId, playerId);
  }
  return roomServiceInstances[key];
}