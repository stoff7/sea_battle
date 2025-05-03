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

        <BattleField>
            
        </BattleField>
         
        <!-- Поле с кораблями доступными-->

            <!-- Кнопка готовности -->
            <button 
                class="ready-btn" 
                :class="{ ready: isReady, 'not-ready': !isReady }" 
                @click="toggleReady"
            >
                {{ isReady ? 'Готов' : 'Не готов' }}
            </button>
        </div>

        <!-- Footer: Кнопка начать игру если роль private -->
        <div class="footer" >
            <button class="start-game-btn" @click="startGame">
                Начать игру
            </button>
        </div>
</template>

<script setup>

</script>

<script>
import BattleField from '@/components/BattleField.vue';
export default {
    name: 'RoomView',
    props: {
        roomId: {
            type: String,
            required: true
        }
    },
    components: {
        BattleField
    },
    data() {
        return {
            participants: [],
            isReady: false,
            privateuserRole: 'private',
            ships: [],
            availableShips: [
                { id:1, name:'4×1', width:160, height:40 },
                { id:2, name:'3×1', width:120, height:40 },
                { id:3, name:'2×1', width:80,  height:40 },
                { id:4, name:'1×1', width:40,  height:40 },],
            ws: null,

        }
    },
    mounted() {
        this.ws = new WebSocket("http://localhost:8080/ws");

        this.ws.onopen = () => {
            console.log("WebSocket соединение установлено");
            // При необходимости можно, например, отправить данные о подключении
            this.ws.send(JSON.stringify({ roomId: this.roomId, action: "join" }));
        };

        this.ws.onmessage = (event) => {
            console.log("Получено сообщение:", event.data);
            // Обработка сообщений от сервера
        };

        this.ws.onerror = error => {
            console.error("Ошибка WebSocket:", error);
        };

        this.ws.onclose = () => {
            console.log("WebSocket соединение закрыто");
        };
    },
    beforeUnmount() {
        if (this.ws) {
            this.ws.close();
        }
    },
    methods: {
        toggleReady() {
            this.isReady = !this.isReady
        },
        startGame() {
            console.log('Игра начинается!')

            this.$router.push({ name: 'inbattle', params: { roomId: this.roomId } })
        },
        handleFieldClick(event) {
            console.log('Клик по полю', event)
        },
        placeShip(ship) {
            const newShip = { ...ship, id: Date.now(), x: 10, y: 10 }
            this.ships.push(newShip)
        },
        toggleShip(ship) {
            const index = this.ships.findIndex(s => s.id === ship.id)
            if (index !== -1) {
                this.ships.splice(index, 1)
            }
        },
        placeShipOnField({ ship, row, col }) {
    const x = col * 40, y = row * 40
    this.ships.push({ ...ship, x, y })  // ship.length точно число
  }
    }
}
</script>

<style scoped>
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
.sidebar th, .sidebar td {
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