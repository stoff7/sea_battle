<template>
    <div class="room-view container">
        <div class="upper">
            <button class="disconnect-btn" @click="disconnect">{{ $t('room.leave') }}</button>
            <div class="room-header">
                <h1>{{ $t('room.game_number') }}: <span>{{ gameId }}</span></h1>
            </div>
            <LanguageButton />
        </div>
        <div class="room-grid" :class="{ 'room-grid-wide': availableShips.length !== 0 }">
            <!-- 1. Колонка: Правила игры -->
            <div class="rules-panel">
                <div class="rules-title">{{ $t('room.rules_title') }}</div>
                <div class="rules-content">
                    {{ $t('room.rules_text') }}
                </div>
            </div>

            <!-- 2. Колонка: Поле и кнопки -->
            <div class="center-panel">
                <div class="main">
                    <div class="battlefield-large">
                        <BattleField :cellPx="48" :available-ships="availableShips" :ships="ships" :ready="isReady"
                            @place-ship="placeShipOnField" @remove-ship="toggleShip" @rotate-ship="onRotateShip" />
                    </div>
                </div>
                <div class="main-buttons">
                    <button class="clear-btn" @click="clearField">{{ $t('room.clear_field') }}</button>
                    <button class="randomize-btn" @click="randomizeShips">{{ $t('room.randomize_ships') }}</button>
                </div>
                <div class="ready-btn-row">
                    <button class="ready-btn" :class="{ ready: isReady, 'not-ready': !isReady }" @click="toggleReady">
                        {{ isReady ? $t('room.ready') : $t('room.not_ready') }}
                    </button>
                </div>
            </div>

            <!-- 3. Колонка: Список игроков и чат -->
            <div class="right-panel">
                <div class="players-table">
                    <h3>{{ $t('room.players_in_room') }}</h3>
                    <table>
                        <thead>
                            <tr>
                                <th>{{ $t('room.player_name') }}</th>
                                <th>{{ $t('room.readiness') }}</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>{{ playerId ? username : '—' }}</td>
                                <td>
                                    <span :style="{ color: isReady ? 'green' : 'red' }">
                                        {{ isReady ? $t('room.table_ready') : $t('room.table_not_ready') }}
                                    </span>
                                </td>
                            </tr>
                            <tr v-if="opponentId">
                                <td>{{ opponentUsername }}</td>
                                <td>
                                    <span :style="{ color: opponentReady ? 'green' : 'red' }">
                                        {{ opponentReady ? $t('room.table_ready') : $t('room.table_not_ready') }}
                                    </span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <Chat :game-id="gameId" :player-id="playerId" :username="username" :ws="wsService.client"
                    :messages="chatStorage.messages" @send="sendChatMessage"
                    :placeholder="$t('room.chat_placeholder')" />
            </div>
        </div>
    </div>
</template>


<script setup>
document.addEventListener('selectstart', e => {
    // если перетаскиваем или находимся над .grid — блокируем
    if (e.target.closest('.grid') || e.target.closest('.ship-placeholder')) {
        e.preventDefault();
    }
});

</script>
<script>
import LanguageButton from '@/components/LanguageButton.vue';
import BattleField from '@/components/BattleField.vue';
import Chat from '@/components/Chat.vue';
import axios from 'axios';
import { wsService } from '@/wsService.js';
import { useUsersStore } from '@/stores/users';
import { useChatStore } from '@/stores/chat';
import { useInBattleStore } from '@/stores/inbattle';

export default {
    name: 'RoomView',
    components: { BattleField, Chat, LanguageButton },
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

        const savedShips = JSON.parse(localStorage.getItem('myShips'));
        if (savedShips) {
            this.ships = savedShips;
            this.availableShips = [];
        }
        else {
            this.randomizeShips();
        }
        window.addEventListener('beforeunload', this.handleBeforeUnload);
        if (this.chatStorage.messages.length !== 0) {
            this.chatMessages = this.chatStorage.messages;
        }
        if (localStorage.getItem('roomState')) this.loadRoomState();
        console.log(this.gameId, this.playerId, this.username, this.role);
    },
    unmounted() {
        wsService.disconnect();
        window.removeEventListener('beforeunload', this.handleBeforeUnload);
    },
    data() {
        const userStorage = useUsersStore();
        const chatStorage = useChatStore();
        const inBattleStore = useInBattleStore();
        return {
            api: import.meta.env.VITE_API,
            username: userStorage.username,
            gridSize: 10,
            stompClient: null,
            opponentUsername: userStorage.opponentName,
            opponentId: userStorage.opponentId,
            opponentReady: false,
            playerId: userStorage.playerId,
            isReady: false,

            role: userStorage.role,
            ships: [],  // размещённые на поле
            availableShips: [
                { id: 1, name: '4×1', w: 4, h: 1, direction: 'horizontal', angle: 90, size: 4 },
                { id: 2, name: '3×1', w: 3, h: 1, direction: 'horizontal', angle: 90, size: 3 },
                { id: 3, name: '3×1', w: 3, h: 1, direction: 'horizontal', angle: 90, size: 3 },
                { id: 4, name: '2×1', w: 2, h: 1, direction: 'horizontal', angle: 90, size: 2 },
                { id: 5, name: '2×1', w: 2, h: 1, direction: 'horizontal', angle: 90, size: 2 },
                { id: 6, name: '2×1', w: 2, h: 1, direction: 'horizontal', angle: 90, size: 2 },
                { id: 7, name: '1×1', w: 1, h: 1, direction: 'horizontal', angle: 90, size: 1 },
                { id: 8, name: '1×1', w: 1, h: 1, direction: 'horizontal', angle: 90, size: 1 },
                { id: 9, name: '1×1', w: 1, h: 1, direction: 'horizontal', angle: 90, size: 1 },
                { id: 10, name: '1×1', w: 1, h: 1, direction: 'horizontal', angle: 90, size: 1 },
            ],
            ws: null,
            userStorage: userStorage,
            chatStorage: chatStorage,
            inBattleStore: inBattleStore,
            chatMessages: [],
        };
    },
    methods: {
        handleBeforeUnload(event) {
            // Синхронный запрос — только так браузер гарантирует выполнение!
            navigator.sendBeacon(
                `https://${this.api}/api/v1/${this.gameId}/leave_game`,
                JSON.stringify({ playerId: this.playerId })
            );
            // Можно не ставить event.preventDefault(), современные браузеры игнорируют кастомные сообщения
        },
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
        onRotateShip(updatedShip) {
            this.ships = this.ships.map(ship =>
                ship.id === updatedShip.id ? updatedShip : ship
            );
            console.log('Повернули корабль', updatedShip);
            this.saveRoomState();
        },
        randomizeShips() {
            if (this.isReady) {
                alert('Сначала снимите готовность!');
                return;
            }
            const directions = [
                [1, 0], [-1, 0], [0, 1], [0, -1],
                [1, 1], [-1, -1], [1, -1], [-1, 1]
            ];
            this.clearField();

            const takenCoords = new Set();

            for (let o = 0; o < 150 && this.availableShips.length > 0; o++) {
                let done = true;
                const randomX = Math.floor(Math.random() * (this.gridSize));
                const randomY = Math.floor(Math.random() * (this.gridSize));
                const randomShip = this.availableShips[Math.floor(Math.random() * this.availableShips.length)];
                let w = randomShip.w;
                let h = randomShip.h;
                // случайное направление
                const direction = Math.random() > 0.5 ? 'horizontal' : 'vertical';
                let newCoords = new Set();

                if (direction === 'horizontal') {
                    console.log(randomShip);
                    randomShip.direction = 'horizontal';
                    for (let j = 0; j < randomShip.w; j++) {
                        const key = [randomX + j, randomY].toString();
                        if (takenCoords.has(key) || randomX + j >= this.gridSize || randomY >= this.gridSize) {
                            done = false;
                            break;
                        }
                        newCoords.add([randomX + j, randomY]);
                    }
                } else {
                    console.log(randomShip);
                    randomShip.direction = 'vertical';
                    [w, h] = [h, w];
                    for (let j = 0; j < randomShip.w; j++) {
                        const key = [randomX, randomY + j].toString();
                        if (takenCoords.has(key) || randomX >= this.gridSize || randomY + j >= this.gridSize) {
                            done = false;
                            break;
                        }
                        newCoords.add([randomX, randomY + j]);
                    }

                }

                if (done) {
                    randomShip.direction = direction;
                    randomShip.w = w;
                    randomShip.h = h;
                    newCoords.forEach(([x, y]) => {
                        takenCoords.add([x, y].toString());
                        directions.forEach(([dx, dy]) => {
                            takenCoords.add([x + dx, y + dy].toString());
                        });
                    });
                    console.log('Корабль', randomShip, 'направление', direction, 'координаты', Array.from(newCoords));
                    this.placeShipOnField({
                        ship: { ...randomShip, direction },
                        row: Array.from(newCoords)[0][1],
                        col: Array.from(newCoords)[0][0],
                        grabbedIndex: 0
                    });
                }
            }
            this.saveRoomState();
        },
        saveRoomState() {
            //
        },

        // Загружает все данные комнаты из localStorage
        loadRoomState() {
            //
        },
        clearField() {
            if (this.isReady) {
                alert('Сначала снимите готовность!');
                return;
            }
            this.ships = [];
            this.availableShips = [
                { id: 1, name: '4×1', w: 4, h: 1, direction: 'horizontal', angle: 90, size: 4 },
                { id: 2, name: '3×1', w: 3, h: 1, direction: 'horizontal', angle: 90, size: 3 },
                { id: 3, name: '3×1', w: 3, h: 1, direction: 'horizontal', angle: 90, size: 3 },
                { id: 4, name: '2×1', w: 2, h: 1, direction: 'horizontal', angle: 90, size: 2 },
                { id: 5, name: '2×1', w: 2, h: 1, direction: 'horizontal', angle: 90, size: 2 },
                { id: 6, name: '2×1', w: 2, h: 1, direction: 'horizontal', angle: 90, size: 2 },
                { id: 7, name: '1×1', w: 1, h: 1, direction: 'horizontal', angle: 90, size: 1 },
                { id: 8, name: '1×1', w: 1, h: 1, direction: 'horizontal', angle: 90, size: 1 },
                { id: 9, name: '1×1', w: 1, h: 1, direction: 'horizontal', angle: 90, size: 1 },
                { id: 10, name: '1×1', w: 1, h: 1, direction: 'horizontal', angle: 90, size: 1 },
            ];
            this.saveRoomState();
        },
        async toggleReady() {
            console.log('playerId', this.playerId);
            if (this.availableShips.length > 0) {
                alert('Сначала расставьте все корабли!');
                return;
            }
            localStorage.setItem('myShips', JSON.stringify(this.ships));
            this.isReady = !this.isReady;
            console.log(this.playerId, this.isReady, this.ships);
            try {
                console.log(this.convertShipsForApi(this.ships))
                const response = await axios.post('https://' + this.api + '/api/v1/' + this.gameId + '/ready_game', {
                    playerId: this.playerId,
                    ready: this.isReady,
                    ships: this.convertShipsForApi(this.ships)
                });
            }
            catch (error) {
                console.error('Ожидаем готовность другого игрока', error);
                alert('Ожидаем готовность другого игрока');
            }
            this.saveRoomState();
        },
        handleGameEvent(event) {
            console.log('Событие', event);
            console.log(this.role)
            switch (event.type) {
                case 'playerJoined':
                    if (this.role === 'host') {
                        console.log('opponent', event.data.playerId, event.data.userName);
                        this.opponentId = event.data.playerId
                        this.opponentUsername = event.data.userName
                        console.log('opponentUsername', this.opponentUsername, this.opponentId);
                        this.saveRoomState();
                    }
                    break

                case 'playerReady':
                    if (this.role === 'host') {
                        this.opponentReady = event.data.secondOwnerReady;
                    }
                    else {
                        this.opponentReady = event.data.firstOwnerReady;
                    }
                    if (event.data.firstOwnerReady === event.data.secondOwnerReady && event.data.gameStatus === 'ACTIVE') {
                        this.$router.push({ name: 'inbattle', params: { gameId: this.gameId, myShips: this.ships } });
                    }
                    this.saveRoomState();
                    break

                case 'gameStarted':
                    this.inBattleStore.reset();
                    this.$router.push({ name: 'inbattle', params: { gameId: this.gameId } });
                    break

                case 'playerLeft': {
                    if (event.data.playerId === this.playerId) {
                        console.log('You left the game:', event);
                        this.$router.push({ name: 'home' });
                    } else {
                        console.log('Opponent left the game:', event);
                        alert(`${this.opponentUsername} покинул игру!`);
                        this.opponentId = null;
                        this.opponentName = null;
                        this.role = 'host';
                        this.userStorage.setRole('host');
                    }
                    this.saveRoomState();
                    break;
                }
                case 'chatMessage': {
                    this.chatStorage.addMessage({
                        fromPlayer: event.data.fromPlayer,
                        text: event.data.text,
                        timestamp: event.data.timestamp
                    });
                    this.saveRoomState();
                    break;
                }



                default:
                    console.warn('Неизвестный тип события', event.type)
            }
        },

        convertShipsToCoordinates(ships) {
            return ships.reduce((accumulator, ship) => {
                ship.coords.forEach(([x, y]) => {
                    accumulator.push({ x, y });
                });
                return accumulator;
            }, []);
        },
        convertShipsForApi(ships) {
            // Определи соответствие type по длине корабля
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
        },
        placeShipOnField({ ship, row, col, grabbedIndex }) {
            // вычисляем координаты в пикселях (40px — размер клетки)
            const x0 = col - grabbedIndex;
            const y0 = row;
            // собираем массив координат для Vue-реактивного хранения
            const coords = [];
            if (ship.direction === 'horizontal') {
                for (let i = 0; i < ship.w; i++) {
                    coords.push([x0 + i, y0]);
                }
            } else {
                for (let i = 0; i < ship.h; i++) {
                    coords.push([x0, y0 + i]);
                }
            }
            this.ships.push({
                ...ship,
                id: Date.now() + Math.random(), // уникальный
                coords,
                angle: ship.angle ?? 90
            });
            // если нужно убрать из палитры:
            this.availableShips = this.availableShips.filter(s => s.id !== ship.id);
            this.saveRoomState();
        },
        toggleShip(ship) {
            // удаляем с поля
            this.ships = this.ships.filter(s => s.id !== ship.id);
            // возвращаем в палитру
            this.availableShips.push({
                id: ship.id,
                name: ship.name,
                w: ship.direction === 'horizontal' ? ship.w : ship.h,
                h: ship.direction === 'horizontal' ? ship.h : ship.w,
                direction: 'horizontal'
            });
            this.saveRoomState();
        },
        async sendChatMessage(text) {
            try {
                console.log('Отправляем сообщение в чат:', text);
                console.log('gameId', this.gameId);
                console.log('playerId', this.playerId);
                await axios.post(`https://${this.api}/api/v1/${this.gameId}/chat_message`, {
                    playerId: this.playerId,
                    textMessage: text
                });
                // Не пушим в chatMessages — сообщение придёт через WebSocket!
            } catch (e) {
                alert('Ошибка отправки сообщения');
            }
        },
    }
};
</script>

<style>
/* Фон всей страницы */
body {
    background: url(@/assets/images/sea_battle_background.jpeg) no-repeat center center fixed;
    background-color: #22334a;
    background-size: cover;
    margin: 0;
    padding: 0;
    font-family: 'Segoe UI', 'Roboto', 'Arial', sans-serif;
    min-height: 100vh;
}

/* Контейнер комнаты */
.room-view.container {
    box-sizing: border-box;
    padding: 48px 36px 32px 36px;
    background: transparent;
    position: relative;
    z-index: 1;
    overflow: visible;


}


/* Заголовки */
.room-header h1 {
    font-family: 'Impact', 'Arial Black', sans-serif;
    font-size: 64px;
    color: #e0d8cc;
    letter-spacing: 10px;
    text-transform: uppercase;
    text-align: center;
    font-weight: 900;
    margin: 0 0 18px 0;
    line-height: 1.08;
    filter: drop-shadow(0 6px 24px #12325a);
    background: none;
}

.instruction h2 {
    color: #90caf9;
    margin-bottom: 10px;
    font-weight: 800;
    text-shadow: 0 1px 4px #1565c0;
    font-size: 2em;
    letter-spacing: 2px;
    text-align: center;
}

/* Таблица игроков */
.players-table {
    width: 90%;
    margin-bottom: 0;
    margin-top: 0;
}

.players-table h3 {
    text-align: center;
    color: #90caf9;
    margin-bottom: 10px;
    font-size: 1.2rem;
    font-weight: 700;
    letter-spacing: 1px;
}

.players-table table {
    width: 100%;
    border-collapse: collapse;
    background: #22334a;
    border-radius: 8px;
    overflow: hidden;
}

.players-table th,
.players-table td {
    padding: 10px 8px;
    text-align: center;
    font-size: 1.08rem;
}

.players-table th {
    background: #1565c0;
    color: #e3f2fd;
    font-weight: 800;
    border-bottom: 1px solid #1976d2;
    letter-spacing: 1px;
}

.players-table tr {
    transition: background 0.15s;
}

.players-table tbody tr:nth-child(even) {
    background: #1a2636;
}

.players-table tbody tr:nth-child(odd) {
    background: #22334a;
}

.players-table td {
    color: #b3e5fc;
    font-weight: 600;
}

.players-table span {
    font-weight: 700;
    font-size: 1em;
    letter-spacing: 0.5px;
}

.players-table tr:hover {
    background: #1976d2;
    color: #fff;
}

/* Кнопки */
button,
button.clear-btn,
button.randomize-btn,
button.ready-btn,
button.disconnect-btn,
button.start-game-btn {
    padding: 12px 32px;
    border-radius: 12px;
    font-size: 1.1rem;
    font-weight: 700;
    cursor: pointer;
    border: none;
    transition: background 0.18s, color 0.18s, box-shadow 0.18s;
    box-shadow: 0 2px 8px #1976d244;
    margin: 0 8px;
    background: linear-gradient(120deg, #217dbb 0%, #3498db 100%);
    color: #fff;
    text-shadow: 0 1px 4px #000a;
}

button:hover {
    background: linear-gradient(120deg, #6dd5fa 0%, #217dbb 100%);
    color: #fff;
}

.buttons-row button {
    width: 220px;
    height: 60px;
    min-width: 180px;
    max-width: 100%;
    font-size: 1.08em;
    box-sizing: border-box;
}

.buttons-row button:hover,
.start-game-btn:hover {
    background: linear-gradient(120deg, #6dd5fa 0%, #217dbb 100%);
    color: #fff;
}

.disconnect-btn {
    background: #e53935;
    color: #fff;
    margin-bottom: 12px;
}

.disconnect-btn:hover {
    background: #b71c1c;
}

.ready-btn.ready {
    background: #43a047;
    color: white;
}

.ready-btn.not-ready {
    background: #e53935;
    color: white;
}

/* Основная зона */
.main-row {
    display: flex;
    flex-direction: row;
    align-items: flex-start;
    justify-content: center;
    gap: 48px;
    margin-bottom: 32px;
}

.side-panel {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    justify-content: flex-start;
    min-width: 250px;
    max-width: 420px;
    width: 100%;
    gap: 18px;
}



/* Чат */
.chat-container {
    margin-top: 10px;
    margin-top: 0;
    width: 100%;
    max-width: 100%;
    min-width: 220px;
    min-height: 0;
    height: auto;
    display: flex;
    flex-direction: column;
    transition: width 0.2s;
}

.chat-header {
    background: linear-gradient(90deg, #3498db 0%, #217dbb 100%);
    color: #fff;
    font-weight: 800;
    font-size: 1.2em;
    padding: 14px 22px;
    border-radius: 18px 18px 0 0;
    letter-spacing: 2px;
    text-align: center;
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background: #22334a;
    border-bottom: 1.5px solid #1976d2;
}

.chat-message {
    margin-bottom: 12px;
    color: #e3f2fd;
    animation: fadeIn 0.3s;
    font-size: 1.08em;
}

.chat-message.mine {
    text-align: right;
    color: #90caf9;
}

.author {
    font-weight: 700;
    margin-right: 8px;
}

.text {
    margin-right: 8px;
}

.time {
    font-size: 0.95em;
    color: #b3e5fc;
    opacity: 0.7;
}

.chat-input-row {
    display: flex;
    align-items: center;
    padding: 12px;
    border-top: 1.5px solid #3498db33;
    background: #1a2636;
    border-radius: 0 0 18px 18px;
    gap: 10px;
}

.chat-input {
    flex: 1;
    padding: 12px;
    border-radius: 10px;
    border: 1.5px solid #3498db;
    font-size: 1.08em;
    background: #22334a;
    color: #fff;
}

.send-btn {
    background: #3498db;
    color: #fff;
    border: none;
    border-radius: 10px;
    font-size: 1.3em;
    padding: 0 18px;
    cursor: pointer;
    transition: background 0.2s;
}

.send-btn:disabled {
    background: #b0bec5;
    cursor: not-allowed;
}



.buttons-row {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: flex-start;
    gap: 14px;
    margin: 0 24px 0 0;
    padding-left: 0;
}

.ready-btn-row {
    display: flex;
    justify-content: center;
    margin: 24px 0 0 0;
}

/* Стили для новой разметки */
.room-grid {
    display: grid;
    grid-template-columns: 1fr 1.5fr 1fr;
    gap: 32px;
    align-items: start;
    width: 100%;
    min-height: 70vh;
    font-family: 'Segoe UI', 'Roboto', 'Arial', sans-serif;
}

.room-grid-wide {
    grid-template-columns: 0.8fr 2.4fr 0.8fr;
}

.rules-panel,
.center-panel,
.right-panel {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    justify-content: flex-start;
    background: rgba(34, 51, 74, 0.92);
    border-radius: 18px;
    padding: 28px 18px;
    min-width: 0;
    min-height: 0;
    box-sizing: border-box;
    height: 100%;
}

.rules-title {
    font-size: 1.5em;
    font-weight: 800;
    color: #90caf9;
    margin-bottom: 18px;
    text-align: center;
    letter-spacing: 2px;
}

.rules-content {
    color: #e3f2fd;
    font-size: 1.38em;
    line-height: 1.5;
    text-align: center center;
    vertical-align: middle;
}

.center-panel {
    display: flex;
    flex-direction: column;
    align-items: center;
    /* Центрирует по горизонтали */
    justify-content: center;
    /* Центрирует по вертикали */
    gap: 24px;
    height: 100%;
    width: 100%;
    text-align: center;
}

.main,
.main-buttons,
.ready-btn-row {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}

.ready-btn-row {
    display: flex;
    justify-content: center;
    margin-top: 8px;
}

.right-panel {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 18px;
    height: 100%;
    min-width: 0;
    max-width: 100%;
    box-sizing: border-box;
}

.chat-container {
    flex: 1 1 0;
    width: 100%;
    min-width: 0;
    max-width: 100%;
    min-height: 0;
    height: auto;
    display: flex;
    flex-direction: column;
    transition: width 0.2s;
    box-sizing: border-box;
    margin: 0;
}

.game-id-header {
    width: 100%;
    text-align: center;
    font-size: 1.35em;
    font-weight: 800;
    color: #90caf9;
    letter-spacing: 2px;
    margin-bottom: 18px;
    padding-top: 8px;
    padding-bottom: 8px;
    background: rgba(34, 51, 74, 0.85);
    border-radius: 14px;
    box-shadow: 0 2px 12px #1976d222;
}


.upper {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
    position: relative;
    width: 100%;
}

.disconnect-btn {
    margin-right: 18px;
}

.room-header {
    flex: 1;
    text-align: center;
}

.lang-btn-wrapper {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    min-width: 60px;
    margin-left: 18px;
}

@media (max-width: 1200px) {
    .chat-container {
        min-width: 140px;
        max-width: 100vw;
    }

    .right-panel {
        min-width: 0;
    }
}
</style>