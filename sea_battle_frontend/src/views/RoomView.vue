<template>
    <div class="room-view container">
        <!-- Sidebar: Список участников -->
        <div class="sidebar">
            <h2>Участники</h2>
            <table>
                <thead>
                    <tr>
                        <th>Имя</th>
                        <th>Статус</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="participant in participants" :key="participant.id">
                        <td>{{ participant.name }}</td>
                        <td>
                            <span :class="{ ready: participant.ready, 'not-ready': !participant.ready }">
                                {{ participant.ready ? 'Готов' : 'Не готов' }}
                            </span>
                        </td>
                    </tr>
                </tbody>
            </table>
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

export default {
    name: 'RoomView',
    components: { BattleField },
    props: {
        gameId: { type: String, required: true }
    },
    data() {
        return {
            participants: [],
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
        toggleReady() {
            if (this.availableShips.length > 0) {
                alert('Сначала расставьте все корабли!');
                return;
            }
            this.isReady = !this.isReady;
        },
        startGame() {
            this.$router.push({ name: 'inbattle', params: { gamemId: this.gameId } });
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
.room-view,
.battle-container {
    user-select: none;
}

.container {
    display: flex;
    flex-direction: column;
    padding: 20px;
}

.sidebar {
    border: 1px solid #ccc;
    padding: 10px;
    margin-bottom: 20px;
}

.sidebar table {
    width: 100%;
    border-collapse: collapse;
}

.sidebar th,
.sidebar td {
    border: 1px solid #ccc;
    padding: 5px;
    text-align: left;
}

.ready {
    color: green;
}

.not-ready {
    color: red;
}

/* Main area */
.main {
    border: 1px solid #ccc;
    padding: 10px;
    margin-bottom: 20px;
}


/* Кнопка готовности */
.ready-btn {
    padding: 10px 20px;
    border: none;
    cursor: pointer;
}

.ready-btn.ready {
    background-color: green;
    color: white;
}

.ready-btn.not-ready {
    background-color: red;
    color: white;
}

/* Footer */
.footer {
    text-align: center;
    margin-top: 20px;
}

.start-game-btn {
    padding: 10px 30px;
    font-size: 16px;
    cursor: pointer;
}
</style>