<template>
    <div class="battle-container">
        <!-- Первое поле боя (статическое) -->
        <div class="battle-field static-field">
            <div v-for="index in gridCells" :key="'static-' + index" class="cell">
                {{ index }}
            </div>
        </div>

        <!-- Второе поле боя (интерактивное) -->
        <div class="battle-field interactive-field">
            <div
                v-for="index in gridCells"
                :key="'interactive-' + index"
                class="cell interactive"
                :class="{ hovered: hoveredCell === index, active: activeCells.includes(index) }"
                @mouseenter="hoveredCell = index"
                @mouseleave="hoveredCell = null"
                @click="toggleActive(index)"
            ></div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Размер поля (например, 10x10)
const gridSize = 10
const gridCells = computed(() => gridSize * gridSize)

const hoveredCell = ref(null)
const activeCells = ref([])

function toggleActive(index) {
    if (activeCells.value.includes(index)) {
        activeCells.value = activeCells.value.filter(val => val !== index)
    } else {
        activeCells.value.push(index)
    }
}
</script>

<script>
export default {
    name: "InbattleView",
    props: {
        roomId: {
            type: String,
            required: true
        }
    },
    data() {
        return {
            gridSize: 10,
            hoveredCell: null,
            activeCells: [],
        };
    },
    methods: {
        toggleActive(index) {
            if (this.activeCells.includes(index)) {
                this.activeCells = this.activeCells.filter(val => val !== index);
            } else {
                this.activeCells.push(index);
            }
        }
    }
};
</script>

<style scoped>
.battle-container {
    display: flex;
    justify-content: space-around;
    gap: 20px;
    padding: 20px;
}

.battle-field {
    display: grid;
    grid-template-columns: repeat(10, 30px);
    grid-template-rows: repeat(10, 30px);
    gap: 2px;
    border: 1px solid #000;
}

.cell {
    background: #fff;
    border: 1px solid #ccc;
    display: flex;
    justify-content: center;
    align-items: center;
}

.interactive-field .cell:hover,
.cell.hovered {
    background: grey;
    cursor: pointer;
}

.cell.active {
    background: darkgrey;
}
</style>