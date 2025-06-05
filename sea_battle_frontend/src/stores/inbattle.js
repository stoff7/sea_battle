// /src/stores/inbattle.js
import { defineStore } from 'pinia';

export const useInBattleStore = defineStore('inBattle', {
    state: () => ({
        gameId: null,
        playerShips: [],
        opponentShips: [],
        hits: [],
        misses: [],
        enemyHits: [],
        enemyMisses: [],
        nextPlayerId: null,
        gameStatus: null,
        winner: null,
    }),
    actions: {
        setGameId(gameId) {
            this.gameId = gameId;
        },
        setPlayerShips(ships) {
            this.playerShips = ships;
        },
        setOpponentShips(ships) {
            this.opponentShips = ships;
        },
        addHit(position) {
            this.hits.push(position);
        },
        addMiss(position) {
            this.misses.push(position);
        },
        addEnemyHit(position) {
            this.enemyHits.push(position);
        },
        addEnemyMiss(position) {
            this.enemyMisses.push(position);
        },
        setNextPlayerId(playerId) {
            this.nextPlayerId = playerId;
        },
        setGameStatus(status) {
            this.gameStatus = status;
        },
        setWinner(winner) {
            this.winner = winner;
        },
        reset() {
            this.gameId = null;
            this.playerShips = [];
            this.opponentShips = [];
            this.hits = [];
            this.misses = [];
            this.enemyHits = [];
            this.enemyMisses = [];
            this.nextPlayerId = null;
            this.gameStatus = null;
            this.winner = null;
        }
    }
});