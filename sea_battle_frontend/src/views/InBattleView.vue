<template>
    <div class="battle-page" @dragstart.prevent @dragover.prevent @drop.prevent>
        <LanguageButton class="lang-btn" />
        <button class="disconnect-btn" @click="disconnect">{{ $t('room.leave') }}</button>
        <div class="timer-bar">
            <template v-if="isMyTurn">
                <span>{{ $t('inbattle.your_turn', { seconds: timer }) }}</span>
            </template>
            <template v-else>
                <span>{{ $t('inbattle.enemy_turn') }}</span>
            </template>
            <div class="timer-progress"
                :style="{ width: (timer / 10 * 100) + '%', background: isMyTurn ? '#2196f3' : '#e53935' }">
            </div>

        </div>
        <h2>{{ $t('home.title') }}</h2>
        <div class="fields">
            <!-- Поле противника СЛЕВА -->
            <div>
                <h3>{{ $t('inbattle.enemy_field') }}</h3>
                <div class="enemy-grid">
                    <div v-for="idx in 100" :key="idx" class="enemy-cell" :class="{
                        hit: enemyHits.includes(idx - 1),
                        miss: enemyMisses.includes(idx - 1)
                    }" @click="attack(idx - 1)">
                        <span v-if="enemyHits.includes(idx - 1)">✸</span>
                        <span v-else-if="enemyMisses.includes(idx - 1)">○</span>
                    </div>
                </div>
                <ShipStatusBar :ships="enemyShips" :cellPx="50" />
            </div>
            <!-- ЧАТ ПО ЦЕНТРУ -->
            <Chat :game-id="gameId" :player-id="playerId" :username="username" :messages="chatStorage.messages"
                @send="sendChatMessage" />
            <!-- Мое поле СПРАВА -->
            <div>
                <h3>{{ $t('inbattle.my_field') }}</h3>
                <BattleField :available-ships="[]" :ships="myShips" :ready="true" :hits="inBattleStore.hits"
                    :misses="inBattleStore.misses" />
                <ShipStatusBar :ships="myShipsWithStatus" :cellPx="50" />
            </div>
        </div>

        <!-- Модальное окно окончания игры -->
        <div v-if="showGameFinishedModal" class="modal-overlay">
            <div class="modal-window">
                <h2>{{ $t('inbattle.game_over') }}</h2>
                <div class="modal-actions">
                    <button @click="goHome" class="modal-btn">{{ $t('inbattle.to_home') }}</button>
                    <button @click="playAgain" class="modal-btn">{{ $t('inbattle.play_again') }}</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import ShipStatusBar from '@/components/ShipStatusBar.vue'
import LanguageButton from '@/components/LanguageButton.vue';
import { useRoute } from 'vue-router'
import BattleField from '@/components/BattleField.vue'
import Chat from '@/components/Chat.vue';
import axios from 'axios'
import { wsService } from '@/wsService.js';
import { useUsersStore } from '@/stores/users';
import { useInBattleStore } from '@/stores/inbattle';
import { useChatStore } from '@/stores/chat';

// Получаем корабли из параметров маршрута или моковые
const route = useRoute()
export default {
    name: 'InbattleView',
    components: { BattleField, Chat, ShipStatusBar, LanguageButton },
    data() {
        const userStorage = useUsersStore();
        const inBattleStore = useInBattleStore();
        const chatStorage = useChatStore();
        return {
            gridSize: 10,
            api: import.meta.env.VITE_API,
            username: userStorage.username,
            playerId: userStorage.playerId,
            opponentId: userStorage.opponentId,
            opponentName: userStorage.opponentName,
            enemyHits: [],
            enemyMisses: [],
            nextPlayerId: localStorage.getItem('role') === 'host' ? userStorage.playerId : userStorage.opponentId,
            gameStatus: null,
            attackCellColors: Array(100).fill('#fff'), // Цвета клеток для атаки
            attackBlocked: Array(100).fill(false), // Блокировка клеток для атаки
            myShips: JSON.parse(localStorage.getItem('myShips')) || [],
            enemyShips: [
                { size: 4, sunk: false },
                { size: 3, sunk: false }, { size: 3, sunk: false },
                { size: 2, sunk: false }, { size: 2, sunk: false }, { size: 2, sunk: false },
                { size: 1, sunk: false }, { size: 1, sunk: false }, { size: 1, sunk: false }, { size: 1, sunk: false }
            ],
            userStorage: userStorage,
            inBattleStore: inBattleStore,
            timer: 10,
            timerInterval: null,
            chatStorage,
            showGameFinishedModal: false,
            stopTimer: false,
            endGame: false, // Флаг для отслеживания окончания игры
        }
    },
    props: {
        gameId: { type: String, required: true }
    },
    mounted() {
        wsService.connect(this.api, this.gameId)

        // 2) подписываемся на событие игры
        wsService.subscribe(
            `/topic/games/${this.gameId}`,
            ({ body }) => this.handleGameEvent(JSON.parse(body))
        )
    },
    unmounted() {
        // 3) отключаемся от веб-сокета
        wsService.disconnect()
        if (this.endGame) {
            console.log('Отключаемся от комнаты ВУИГПП', this.gameId);
            this.disconnect();
        }
    },
    computed: {
        isMyTurn() {
            return this.playerId === this.nextPlayerId;
        },
        myShipsWithStatus() {
            return this.myShips.map(ship => ({
                ...ship,
                sunk: ship.coords.every(([x, y]) =>
                    this.inBattleStore.hits.includes(y * this.gridSize + x)
                )
            }));
        }
    },
    watch: {
        isMyTurn(newVal, oldVal) {
            this.clearTimer();
            this.timer = 10;
            this.startTimer();
        },
        timer(newVal) {
            if (newVal === 0) {
                const idx = this.getRandomAvailableIndex(this.enemyHits, this.enemyMisses);
                if (idx !== null) {
                    this.attack(idx);
                }
                this.clearTimer();
            }
        }
    },
    methods: {
        async disconnect() {
            try {
                const response = await axios.post('https://' + this.api + '/api/v1/' + this.gameId + '/leave_game', {
                    playerId: this.playerId,
                });
                console.log('Отключаемся от комнаты', this.gameId);
                console.log('response', response);
                wsService.disconnect();
                this.$router.push({ name: 'home' });
            } catch (error) {
                console.error('Ошибка при отключении от комнаты:', error);
            }
        },
        startTimer() {
            this.clearTimer();
            if (!this.isMyTurn || this.stopTimer) return;
            this.timerInterval = setInterval(() => {
                if (!this.isMyTurn || this.stopTimer) {
                    this.clearTimer();
                    return;
                }
                if (this.timer > 0) {
                    this.timer--;
                }
            }, 1000);
        },
        clearTimer() {
            if (this.timerInterval) {
                clearInterval(this.timerInterval);
                this.timerInterval = null;
                // Для отладки:
                console.log('Таймер остановлен');
            }
        },
        async sendChatMessage(text) {
            try {
                await axios.post(`https://${this.api}/api/v1/${this.gameId}/chat_message`, {
                    playerId: this.playerId,
                    textMessage: text
                });
            } catch (e) {
                alert('Ошибка отправки сообщения');
            }
        },
        handleGameEvent(event) {
            console.log('Game event:', event);
            switch (event.type) {
                case 'gameFinished':
                    this.stopTimer = true;
                    this.clearTimer();
                    console.log('Game finished:', event);
                    this.endGame = true;
                    this.showGameFinishedModal = true;

                    break;
                case 'shotFired': {
                    const idx = event.data.y * this.gridSize + event.data.x;
                    console.log('Shot fired:', event);
                    console.log('Shot fired at index:', idx);
                    this.nextPlayerId = event.data.nextPlayerId;
                    if (event.data.by !== this.playerId) {
                        // Соперник стреляет по твоему полю
                        if (event.data.result === 'HIT') {
                            console.log('Hit detected:', event);
                            if (!this.inBattleStore.hits.includes(idx)) {
                                this.inBattleStore.addHit(idx);
                                if (event.data.shipState === "KILL") {
                                    const ship = this.myShips.find(s =>
                                        s.coords.some(([x, y]) => x === event.data.x && y === event.data.y)
                                    );
                                    const adjCells = this.getAdjacentCells(ship.coords, this.gridSize);
                                    adjCells.forEach(idx => {
                                        if (!this.enemyMisses.includes(idx)) {
                                            this.enemyMisses.push(idx);
                                        }
                                    });
                                }
                                this.saveBattleProgress();
                                console.log('Hit added:', idx);
                            }
                        } else if (event.data.result === 'MISS') {
                            if (!this.inBattleStore.misses.includes(idx)) {
                                this.inBattleStore.addMiss(idx);
                                console.log('Miss added:', idx, this.inBattleStore.misses);
                                this.saveBattleProgress();
                            }
                        }
                    }
                    // Если стрелял ты — попадания по enemyHits/enemyMisses уже обрабатываются в attack()
                    break;
                }
                case 'playerLeft': {
                    if (event.data.playerId === this.playerId) {
                        console.log('You left the game:', event);
                        this.$router.push({ name: 'home' });
                    } else {
                        console.log('Opponent left the game:', event);
                        alert(`${this.opponentName} покинул игру!`);
                        this.opponentId = null;
                        this.opponentName = null;
                        this.userStorage.setOpponentId(null);
                        this.userStorage.setOpponentName(null);
                        localStorage.setItem('role', 'host');
                        this.$router.push({ name: 'room', params: { gameId: this.gameId } });
                    }
                }
                case 'chatMessage': {
                    this.chatStorage.addMessage({
                        fromPlayer: event.data.fromPlayer,
                        text: event.data.text,
                        timestamp: event.data.timestamp
                    });
                    break;
                }
                default:
                    console.warn('Unknown event type:', event.type);
            }
        },
        goHome() {
            this.showGameFinishedModal = false;
            this.chatStorage.clear();
            this.inBattleStore.reset();
            this.userStorage.reset();
            this.$router.push({ name: 'home' });
        },
        async playAgain() {
            localStorage.removeItem('enemyHits');
            localStorage.removeItem('enemyMisses');
            localStorage.removeItem('myHits');
            localStorage.removeItem('myMisses');
            localStorage.removeItem('myShipsWithStatus');
            localStorage.removeItem('enemyShips');
            this.showGameFinishedModal = false;
            try {
                console.log('Starting replay for game:', this.playerId, this.gameId);
                const response = await axios.patch(`https://${this.api}/api/v1/${this.gameId}/replay_game`, {
                    playerId: this.playerId
                });
                console.log('Replay response:', response.data);
                // Обработка ответа сервера
                const { gameId, playerId, gameStatus, hostName, hostId, message } = response.data;
                if (message) {
                    alert(message);
                    this.goHome();
                    return;
                }
                // Переход в новую игру

                if (hostId === this.playerId) {
                    this.userStorage.setRole('host');
                    localStorage.setItem('role', 'host');
                    this.userStorage.setPlayerId(playerId);
                    this.userStorage.setUsername(this.username);
                    this.userStorage.setOpponentId(null);
                    this.userStorage.setOpponentName(null);
                } else {
                    this.userStorage.setRole('guest');
                    localStorage.setItem('role', 'guest');
                    this.userStorage.setOpponentId(hostId);
                    this.userStorage.setOpponentName(hostName);
                    this.userStorage.setPlayerId(playerId);
                    this.userStorage.setUsername(this.username);
                }
                this.inBattleStore.reset();
                localStorage.removeItem('roomState');
                wsService.disconnect();
                this.$router.push({ name: 'room', params: { gameId: gameId } });
                return;
            } catch (e) {
                console.error('Error finishing game:', e);
                alert('Ошибка при создании новой игры');
                this.$router.push({ name: 'home' });
            }
            // Если не удалось создать новую игру — возвращаем на главную

        },
        textToSize(text) {
            switch (text) {
                case 'FOUR_DECK':
                    return 4;
                case 'THREE_DECK':
                    return 3;
                case 'TWO_DECK':
                    return 2;
                case 'ONE_DECK':
                    return 1;
            }
        },
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
        },
        async attack(idx) {
            console.log(this.nextPlayerId)
            console.log(localStorage.getItem('enemyHits'))
            console.log(this.playerId)
            // 1. Не даём атаковать заблокированные клетки
            if (this.playerId !== this.nextPlayerId) {
                console.warn('Not your turn!');
                return;
            }
            console.log(`Attacking cell index: ${idx}`);
            if (this.attackBlocked[idx]) return;

            // 2. Вычисляем координаты x, y
            const x = idx % this.gridSize;
            const y = Math.floor(idx / this.gridSize);
            console.log(`Attack at (${x},${y})`);
            try {
                // 3. Делаем PATCH-запрос на /api/v1/{gameId}/fight
                const url = 'https://' + this.api + '/api/v1/' + this.gameId + '/fight';
                const payload = {
                    playerId: this.playerId,
                    coord: { x, y }
                };
                console.log(payload);
                const response = await axios.patch(url, payload);


                // 4. Получаем данные из ответа
                const { coord: newCoord, nextPlayerId, gameStatus, message, state } = response.data;
                console.log('Response:', response.data);

                // 5. Обновляем цвет клетки в зависимости от результата
                //    Например, HIT — красим в красный, MISS — в серый
                if (state === 'HIT') {
                    if (!this.enemyHits.includes(idx)) {
                        this.enemyHits = [...this.enemyHits, idx];
                        this.saveBattleProgress();
                    }
                    if (response.data.shipState == "KILL") {

                        const killedShip = this.enemyShips.find(s => !s.sunk && s.size === this.textToSize(response.data.resultShipType));
                        if (killedShip) {
                            killedShip.sunk = true;
                        }
                        const adjCells = this.getAdjacentCells(response.data.resultShipCoords, this.gridSize);
                        adjCells.forEach(idx => {
                            if (!this.enemyMisses.includes(idx)) {
                                this.enemyMisses.push(idx);
                            }
                        });
                    }
                    this.clearTimer();
                    this.timer = 10;
                    this.startTimer();
                } else {
                    if (!this.enemyMisses.includes(idx)) {
                        this.enemyMisses = [...this.enemyMisses, idx];
                        this.saveBattleProgress();
                    }
                }


                // 6. Показываем сообщение пользователю (кратко)
                console.log(`Attack at (${x},${y}) → ${state}`);
                if (message) {
                    alert(message);
                }

                // 7. Опционально: сохраняем новый ход и статус игры
                this.nextPlayerId = nextPlayerId;
                this.gameStatus = gameStatus;

            } catch (error) {
                // 8. Обработка ошибок сети и сервера
                if (error.response) {
                    // Сервер вернул ошибку (4xx, 5xx)
                    console.error('Ошибка от сервера:', error.response.data);
                    alert(`Ошибка: ${error.response.data.message || 'Server error'}`);
                } else if (error.request) {
                    // Запрос ушёл, но ответа нет
                    console.error('Нет ответа от сервера:', error.request);
                    alert('Нет ответа от сервера');
                } else {
                    // Что-то пошло не так на клиенте
                    console.error('Ошибка при запросе:', error.message);
                    alert(`Ошибка: ${error.message}`);
                }
            }
        },
        getRandomAvailableIndex(hits, misses) {
            const all = Array.from({ length: 100 }, (_, i) => i);
            const used = new Set([...(hits || []), ...(misses || [])]);
            const available = all.filter(idx => !used.has(idx));
            if (available.length === 0) return null;
            const randomIdx = Math.floor(Math.random() * available.length);
            return available[randomIdx];
        },
        saveBattleProgress() {
            //
        },
        loadBattleProgress() {
            //
        },
    }
};
</script>

<style scoped>
/* Основной фон страницы */
body {
    background: linear-gradient(120deg, #0f2027 0%, #2c5364 100%);
    min-height: 100vh;
    margin: 0;
    font-family: 'Segoe UI', 'Roboto', 'Arial', sans-serif;
    overflow-x: hidden;
}

/* Контейнер боя */
.battle-page {
    max-width: 1400px;
    margin: 36px auto 0 auto;
    padding: 48px 36px 32px 36px;
    background: rgba(30, 50, 80, 0.92);
    border-radius: 24px;
    box-shadow: 0 12px 48px 0 #1976d2aa, 0 1.5px 0 #fff2 inset;
    color: #e3f2fd;
    position: relative;
    z-index: 1;
    overflow: visible;
}

/* Волны внизу */
.battle-page::after {
    content: "";
    display: block;
    width: 100%;
    height: 64px;
    background: url('https://svgshare.com/i/13r6.svg') repeat-x bottom;
    background-size: contain;
    margin-top: 40px;
    opacity: 0.45;
}

/* Заголовки */
h2 {
    text-align: center;
    letter-spacing: 2px;
    font-size: 2.5em;
    color: #90caf9;
    text-shadow: 0 2px 12px #1976d2cc;
    margin-bottom: 28px;
    font-weight: 800;
}

h3 {
    color: #29b6f6;
    text-shadow: 0 1px 4px #1565c0;
    margin-bottom: 10px;
    letter-spacing: 1px;
    font-weight: 700;
}

/* Таймер */
.timer-bar {
    margin: 0 auto 18px auto;
    max-width: 600px;
    background: rgba(33, 150, 243, 0.13);
    border-radius: 12px;
    padding: 12px 24px 18px 24px;
    box-shadow: 0 2px 12px 0 #1976d244;
    font-size: 1.25em;
    font-weight: 500;
    position: relative;
    overflow: hidden;
}

.timer-progress {
    height: 7px;
    border-radius: 4px;
    margin-top: 10px;
    transition: width 0.3s, background 0.3s;
    box-shadow: 0 1px 6px #1976d288;
}

/* Поля */
.fields {
    display: flex;
    flex-direction: row;
    gap: 72px;
    justify-content: center center;
    align-items: center center;
    background: rgba(33, 150, 243, 0.09);
    border-radius: 18px;
    padding: 36px 28px;
    box-shadow: 0 2px 16px 0 #2196f344;
    margin-bottom: 18px;
    position: relative;
}

.my-field,
.enemy-field {
    display: flex;
    flex-direction: column;
    align-items: center;
    min-width: 420px;
    max-width: 480px;
    background: rgba(255, 255, 255, 0.04);
    border-radius: 14px;
    box-shadow: 0 2px 12px #1976d222;
    padding: 18px 10px 24px 10px;
    position: relative;
}

.enemy-grid {
    display: grid;
    grid-template-columns: repeat(10, 40px);
    grid-template-rows: repeat(10, 40px);
    gap: 0;
    background: linear-gradient(135deg, #1976d2 60%, #64b5f6 100%);
    border: 2.5px solid #1565c0;
    border-radius: 12px;
    box-shadow: 0 2px 12px #1976d2aa;
    margin-top: 10px;
}

.enemy-cell {
    width: 40px;
    height: 40px;
    border: 1.5px solid #42a5f5;
    background: rgba(255, 255, 255, 0.93);
    transition: background 0.18s, box-shadow 0.18s;
    box-shadow: 0 1px 2px #1976d222;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 1.3em;
    user-select: none;
}

.enemy-cell.hit {
    background: radial-gradient(circle, #e53935 60%, #b71c1c 100%);
    color: #fff;
    box-shadow: 0 0 12px #e53935cc;
    animation: splash-hit 0.4s;
}

.enemy-cell.miss {
    background: repeating-linear-gradient(135deg,
            #b0bec5 0px,
            #b0bec5 10px,
            #cfd8dc 10px,
            #cfd8dc 20px);
    color: #607d8b;
    animation: splash-miss 0.4s;
}

.enemy-cell:hover:not(.hit):not(.miss) {
    background: linear-gradient(135deg, #81d4fa 60%, #29b6f6 100%);
    box-shadow: 0 0 12px #29b6f6cc;
    z-index: 2;
}

@keyframes splash-hit {
    0% {
        box-shadow: 0 0 0 #e5393500;
    }

    60% {
        box-shadow: 0 0 32px #e53935cc;
    }

    100% {
        box-shadow: 0 0 12px #e53935cc;
    }
}

@keyframes splash-miss {
    0% {
        box-shadow: 0 0 0 #b0bec500;
    }

    60% {
        box-shadow: 0 0 16px #b0bec5cc;
    }

    100% {
        box-shadow: 0 0 4px #b0bec5cc;
    }
}

/* Чат */
.chat-container {
    width: 480px;
    min-width: 320px;
    max-width: 640px;
    margin: 0 48px;
    background: rgba(26, 38, 54, 0.98);
    border-radius: 16px;
    box-shadow: 0 4px 24px #217dbb33;
    display: flex;
    flex-direction: column;
    height: 660px;
    align-self: center center;
    border: 2px solid #1976d2;
}

@media (max-width: 1200px) {
    .fields {
        gap: 24px;
        padding: 18px 4px;
    }

    .my-field,
    .enemy-field,
    .chat-container {
        min-width: 0;
        width: 100%;
        max-width: 100vw;
    }

    .chat-container {
        margin: 18px 0;
        height: 340px;
    }
}

@media (max-width: 900px) {
    .fields {
        flex-direction: column;
        align-items: stretch;
        gap: 18px;
    }

    .chat-container {
        margin: 0 auto 18px auto;
        width: 100%;
        min-width: 0;
    }
}

/* Модальное окно */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-window {
    background: #2c3e50;
    border-radius: 16px;
    padding: 36px 32px 28px 32px;
    max-width: 420px;
    width: 100%;
    box-shadow: 0 8px 32px #1976d2aa;
    animation: slide-in 0.4s ease-out;
    text-align: center;
}

.modal-actions {
    display: flex;
    justify-content: space-between;
    margin-top: 24px;
}

.modal-btn {
    flex: 1;
    margin: 0 12px;
    padding: 14px;
    background: #3498db;
    color: #fff;
    border: none;
    border-radius: 10px;
    cursor: pointer;
    font-size: 1.1em;
    font-weight: 600;
    transition: background 0.3s, transform 0.3s;
    box-shadow: 0 2px 8px #1976d244;
}

.modal-btn:hover {
    background: #2980b9;
    transform: translateY(-2px) scale(1.04);
}

@keyframes slide-in {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.ships-visual {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 12px;
}

.ship-icon {
    font-size: 2em;
    color: #90caf9;
    transition: color 0.3s;
}

.ship-icon.sunk {
    color: #e53935;
    filter: drop-shadow(0 0 6px #e53935aa);
}

.lang-btn {
    position: absolute;
    top: 15px;
    right: 24px;
    z-index: 10;
}
</style>