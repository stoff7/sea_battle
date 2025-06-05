/**
 * Фасад для работы с localStorage в морском бое.
 * Все ключи и структура хранения централизованы здесь.
 */
const STORAGE_KEYS = {
  roomState: 'roomState',
  myShips: 'myShips',
  isReady: 'isReady',
  playerId: 'playerId',
  enemyHits: 'enemyHits',
  enemyMisses: 'enemyMisses',
  myHits: 'myHits',
  myMisses: 'myMisses',
  myShipsWithStatus: 'myShipsWithStatus',
  enemyShips: 'enemyShips',
  nextPlayerId: 'nextPlayerId',
  username: 'username',
};

export const storage = {
  // --- Универсальные методы ---
  save(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
  },
  load(key, def = null) {
    const v = localStorage.getItem(key);
    return v ? JSON.parse(v) : def;
  },
  remove(key) {
    localStorage.removeItem(key);
  },

  // --- Специализированные методы для игры ---
  saveRoomState(state) {
    this.save(STORAGE_KEYS.roomState, state);
  },
  loadRoomState() {
    return this.load(STORAGE_KEYS.roomState, null);
  },
  removeRoomState() {
    this.remove(STORAGE_KEYS.roomState);
  },

  saveMyShips(ships) {
    this.save(STORAGE_KEYS.myShips, ships);
  },
  loadMyShips() {
    return this.load(STORAGE_KEYS.myShips, []);
  },
  removeMyShips() {
    this.remove(STORAGE_KEYS.myShips);
  },

  saveIsReady(isReady) {
    this.save(STORAGE_KEYS.isReady, isReady);
  },
  loadIsReady() {
    return this.load(STORAGE_KEYS.isReady, false);
  },
  removeIsReady() {
    this.remove(STORAGE_KEYS.isReady);
  },

  // ...добавь аналогичные методы для других ключей...

  // --- Массовое удаление для новой игры ---
  clearAllGameData() {
    Object.values(STORAGE_KEYS).forEach(key => localStorage.removeItem(key));
  }
};