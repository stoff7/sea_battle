import { getApiFacade } from './apiFacade';

class GameService {
  constructor(api, gameId, playerId) {
    this.api = api;
    this.gameId = gameId;
    this.playerId = playerId;
    this.apiFacade = getApiFacade(`https://${api}/api/v1`);
  }

  async leaveGame() {
    return this.apiFacade.leaveGame(this.api, this.gameId, this.playerId);
  }

  async sendChatMessage(text) {
    return this.apiFacade.sendChatMessage(this.api, this.gameId, this.playerId, text);
  }

  async fight(coord) {
    return this.apiFacade.fight(this.api, this.gameId, this.playerId, coord);
  }

  async replayGame() {
    
    return this.apiFacade.replayGame(this.api, this.gameId, this.playerId);
  }

  getAdjacentCells(coords, gridSize = 10) {
    const adj = new Set();
    coords.forEach(({ x, y }) => {
      for (let dx = -1; dx <= 1; dx++) {
        for (let dy = -1; dy <= 1; dy++) {
          const nx = x + dx;
          const ny = y + dy;
          const idx = ny * gridSize + nx;
          if (
            nx >= 0 && nx < gridSize &&
            ny >= 0 && ny < gridSize &&
            !coords.some(c => c.x === nx && c.y === ny)
          ) {
            adj.add(idx);
          }
        }
      }
    });
    return Array.from(adj);
  }

  textToSize(text) {
    switch (text) {
      case 'FOUR_DECK': return 4;
      case 'THREE_DECK': return 3;
      case 'TWO_DECK': return 2;
      case 'ONE_DECK': return 1;
    }
  }

  getRandomAvailableIndex(hits, misses) {
    const all = Array.from({ length: 100 }, (_, i) => i);
    const used = new Set([...(hits || []), ...(misses || [])]);
    const available = all.filter(idx => !used.has(idx));
    if (available.length === 0) return null;
    const randomIdx = Math.floor(Math.random() * available.length);
    return available[randomIdx];
  }
}

// Singleton для каждого боя (по gameId)
const gameServiceInstances = {};
export function getGameService(api, gameId, playerId) {
  const key = `${api}_${gameId}_${playerId}`;
  if (!gameServiceInstances[key]) {
    gameServiceInstances[key] = new GameService(api, gameId, playerId);
  }
  return gameServiceInstances[key];
}