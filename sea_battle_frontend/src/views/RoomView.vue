<template>
    <div class="room-view container">
        <!-- Room Number -->
        <div class="room-header">
            <h1>НОМЕР КОМНАТЫ: <span class="room-id">{{ gameId }}</span></h1>
        </div>

        <!-- Instruction -->
        <div class="instruction">
            <h2>РАССТАВЬТЕ СВОИ КОРАБЛИ!</h2>
        </div>

        <!-- Main area: Поле для расстановки кораблей -->
        <div class="main">
            <BattleField :available-ships="availableShips" :ships="ships" :ready="isReady"
                @place-ship="placeShipOnField" @remove-ship="toggleShip" />
        </div>

        <!-- Кнопка готовности -->
        <button class="ready-btn" :class="{ ready: isReady, 'not-ready': !isReady }" @click="toggleReady">
            {{ isReady ? 'Готов' : 'Не готов' }}
        </button>

        <!-- Footer: Кнопка начать игру если роль private -->
        <div class="footer">
            <button class="start-game-btn" @click="startGame">
                Начать игру
            </button>
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
import BattleField from '@/components/BattleField.vue';
import axios from 'axios';
import { wsService } from '@/wsService.js';
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import { Client } from '@stomp/stompjs';

export default {
    name: 'RoomView',
    components: { BattleField },
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
    data() {
        return {
            api: import.meta.env.VITE_API,
            stompClient: null,
            participants: [],
            opponentId: null,
            opponentReady: false,
            playerId: localStorage.getItem('playerId'),
            isReady: false,
            ships: [],  // размещённые на поле
            availableShips: [
                { id: 1, name: '4×1', w: 4, h: 1 },
                { id: 2, name: '3×1', w: 3, h: 1 },
                { id: 3, name: '3×1', w: 3, h: 1 },
                { id: 4, name: '2×1', w: 2, h: 1 },
                { id: 5, name: '2×1', w: 2, h: 1 },
                { id: 6, name: '2×1', w: 2, h: 1 },
                { id: 7, name: '1×1', w: 1, h: 1 },
                { id: 8, name: '1×1', w: 1, h: 1 },
                { id: 9, name: '1×1', w: 1, h: 1 },
                { id: 10, name: '1×1', w: 1, h: 1 },
            ],
            ws: null,
        };
    },
    methods: {
        async toggleReady() {
            console.log('playerId', this.playerId);
            if (this.availableShips.length > 0) {
                alert('Сначала расставьте все корабли!');
                return;
            }
            localStorage.setItem('myShips', JSON.stringify(this.ships));
            this.isReady = !this.isReady;
            console.log(this.playerId, this.isReady, this.ships);
            const response = await axios.post('https://' + this.api + '/api/v1/' + this.gameId + '/ready_game', {
                playerId: this.playerId,
                ready: this.isReady,
                cells: this.convertShipsToCoordinates(this.ships)
            });
            console.log(response.data)
        },
        handleGameEvent(event) {
            console.log('Событие', event);
            switch (event.type) {
                case 'playerJoined':
                    // пришёл второй игрок
                    this.opponentId = event.playerId
                    break

                case 'playerReady':
                    // обновляем статусы готовности
                    // event.firstOwnerReady, event.secondOwnerReady
                    if (event.firstOwnerReady === event.secondOwnerReady && event.gameStatus === 'ACTIVE') {
                        this.$router.push({ name: 'inbattle', params: { gameId: this.gameId, myShips: this.ships } });
                    }
                    break

                case 'gameStarted':
                    this.$router.push({ name: 'inbattle', params: { gameId: this.gameId, myShips: this.ships } });
                    break

                case 'shotFired':
                    // сохраняем выстрел, можно отрисовать на доске
                    this.shots.push({
                        x: event.x,
                        y: event.y,
                        result: event.result,
                        by: event.by
                    })
                    // сохраняем чей ход следующий
                    this.nextPlayerId = event.nextPlayerId
                    break

                case 'gameFinished':
                    this.$router.push({ name: 'home' });
                    break

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
        placeShipOnField({ ship, row, col, grabbedIndex }) {
            // вычисляем координаты в пикселях (40px — размер клетки)
            const x0 = col - grabbedIndex;
            const y0 = row;
            // собираем массив координат для Vue-реактивного хранения
            const coords = [];
            for (let i = 0; i < ship.w; i++) {
                coords.push([x0 + i, y0]);
            }
            this.ships.push({
                ...ship,
                id: Date.now() + Math.random(), // уникальный
                coords
            });
            // если нужно убрать из палитры:
            this.availableShips = this.availableShips.filter(s => s.id !== ship.id);
        },
        toggleShip(ship) {
            // удаляем с поля
            this.ships = this.ships.filter(s => s.id !== ship.id);
            // возвращаем в палитру
            this.availableShips.push({
                id: ship.id,
                name: ship.name,
                w: ship.coords.length,
                h: 1
            });
        }
    }
};
</script>

<style scoped>
.room-header {
    text-align: center;
    margin-bottom: 18px;
    background: linear-gradient(90deg, #1976d2 0%, #42a5f5 100%);
    padding: 18px 0 10px 0;
    border-radius: 10px 10px 0 0;
    box-shadow: 0 2px 8px rgba(25, 118, 210, 0.08);
}

.room-header h1 {
    color: #fff;
    font-size: 2.1rem;
    letter-spacing: 2px;
    margin: 0;
}

.room-id {
    font-weight: bold;
    background: #fff;
    color: #1976d2;
    padding: 2px 12px;
    border-radius: 8px;
    margin-left: 10px;
    font-size: 1.2em;
    box-shadow: 0 1px 4px rgba(25, 118, 210, 0.10);
}

.instruction {
    text-align: center;
    margin-bottom: 22px;
}

.instruction h2 {
    color: #1976d2;
    font-size: 1.5rem;
    letter-spacing: 1px;
    margin: 0;
    font-weight: 600;
    text-shadow: 0 2px 8px rgba(25, 118, 210, 0.10);
}

.main {
    border: 1px solid #1976d2;
    background: #f5faff;
    padding: 18px;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 24px;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(25, 118, 210, 0.08);
}

.ready-btn {
    padding: 12px 28px;
    border: none;
    cursor: pointer;
    font-size: 1.1rem;
    border-radius: 8px;
    margin: 0 auto 18px auto;
    display: block;
    transition: background 0.2s, box-shadow 0.2s;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(25, 118, 210, 0.08);
}

.ready-btn.ready {
    background-color: #43a047;
    color: white;
}

.ready-btn.not-ready {
    background-color: #e53935;
    color: white;
}

.footer {
    text-align: center;
    margin-top: 18px;
}

.start-game-btn {
    padding: 12px 36px;
    font-size: 1.15rem;
    cursor: pointer;
    background-color: #1976d2;
    color: #fff;
    border: none;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(25, 118, 210, 0.08);
    transition: background 0.2s, box-shadow 0.2s;
    font-weight: 600;
}

.start-game-btn:hover {
    background-color: #1565c0;
    box-shadow: 0 4px 16px rgba(25, 118, 210, 0.15);
}

.start-game-btn:active {
    background-color: #0d47a1;
}
</style>