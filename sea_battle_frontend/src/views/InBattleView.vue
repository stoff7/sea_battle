<template>
    <div class="battle-page" @dragstart.prevent @dragover.prevent @drop.prevent>
        <h2>Морской бой</h2>
        <div class="fields">
            <!-- Мое поле -->
            <div>
                <h3>Мое поле</h3>
                <BattleField :available-ships="[]" :ships="myShips" :ready="true" />
            </div>
            <!-- Поле противника -->
            <div>
                <h3>Поле противника</h3>
                <div class="enemy-grid">
                    <div v-for="idx in 100" :key="idx" class="enemy-cell" :class="{
                        hit: enemyHits.has(idx - 1),
                        miss: enemyMisses.has(idx - 1)
                    }" @click="attack(idx - 1)">
                        <span v-if="enemyHits.has(idx - 1)">✸</span>
                        <span v-else-if="enemyMisses.has(idx - 1)">•</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>

import { useRoute } from 'vue-router'
import BattleField from '@/components/BattleField.vue'
import axios from 'axios'

// Получаем корабли из параметров маршрута или моковые
const route = useRoute()
</script>

<script>
export default {
    name: 'InbattleView',
    components: { BattleField },
    data() {
        return {
            participants: [],
            gridSize: 10,
            api: 'https://' + import.meta.env.VITE_API,
            playerId: localStorage.getItem('playerId'),
            isReady: false,
            enemyHits: new Set(),
            enemyMisses: new Set(),
            nextPlayerId: null,
            gameStatus: null,
            attackCellColors: Array(100).fill('#fff'), // Цвета клеток для атаки
            attackBlocked: Array(100).fill(false), // Блокировка клеток для атаки
            myShips: JSON.parse(localStorage.getItem('myShips')),
        }
    },
    props: {
        gameId: { type: String, required: true }
    },
    mounted() {
        // const ships = JSON.parse(localStorage.getItem('myShips'));
        // console.log(ships); // Получаем корабли из локального хранилища
    },
    methods: {
        async attack(idx) {
            // 1. Не даём атаковать заблокированные клетки
            if (this.attackBlocked[idx]) return;

            // 2. Вычисляем координаты x, y
            const x = idx % this.gridSize;
            const y = Math.floor(idx / this.gridSize);
            console.log(`Attack at (${x},${y})`);

            try {
                // 3. Делаем PATCH-запрос на /api/v1/{gameId}/fight
                const url = this.api + '/api/v1/' + this.gameId + '/fight';
                const payload = {
                    playerId: this.playerId,
                    coord: { x, y }
                };
                console.log(payload);
                const response = await axios.patch(url, payload);
                // axios.patch(url, data) — стандартный способ частичного обновления ресурса :contentReference[oaicite:0]{index=0}

                // 4. Получаем данные из ответа
                const { coord: newCoord, nextPlayerId, gameStatus, message, state } = response.data;
                console.log('Response:', response.data);

                // 5. Обновляем цвет клетки в зависимости от результата
                //    Например, HIT — красим в красный, MISS — в серый
                if (state === 'HIT') {
                    this.enemyHits.add(idx);
                } else {
                    this.enemyMisses.add(idx);
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
        }
    }
};
</script>

<style scoped>
.battle-page {
    max-width: 900px;
    margin: 0 auto;
    padding: 24px;
}

.fields {
    display: flex;
    gap: 48px;
    justify-content: center;
}

.enemy-grid {
    display: grid;
    grid-template-columns: repeat(10, 40px);
    grid-template-rows: repeat(10, 40px);
    gap: 0;
    background: #eef;
    border: 1px solid #1976d2;
    margin-top: 8px;
}

.enemy-cell {
    width: 40px;
    height: 40px;
    border: 1px solid #bbb;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 1.3em;
    user-select: none;
    transition: background 0.15s;
}

.enemy-cell.hit {
    background: #e53935;
}

.enemy-cell.miss {
    background: #b0bec5;
}


.enemy-cell:hover:not(.hit):not(.miss) {
    background: #bbdefb;
}
</style>
