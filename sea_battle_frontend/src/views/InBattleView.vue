<template>
    <div class="grid" @dragover.prevent @dragenter.prevent>
        <div v-for="(cell, index) in gridSize * gridSize" :key="index" class="cell" @dragover.prevent
            @drop="onDrop($event, index)">
        </div>
    </div>

    <div class="palette">
        <div v-for="it in palette" :key="it.id" class="palette-item" draggable="true"
            @dragstart="onDragStart(it, null, $event)">
            {{ it.label }} ({{ it.w }}×{{ it.h }})
        </div>
    </div>

    <!-- Корабли -->
    <Ship v-for="ship in ships" :key="ship.id" :id="ship.id" :label="ship.label" :coords="ship.coords"
        :grabbedIndex="0" />
</template>


<script setup>
import { ref } from 'vue'
import Ship from '@/components/Ship.vue'

const gridSize = 10
const ships = ref([
    // из палитры сюда добавятся корабли в виде:
    // { id: '1', label: 'A', w: 2, h: 1, coords: [[x0,y0], [x1,y1]] }
])

const palette = [
    { id: '1', label: 'A', w: 1, h: 1 },
    { id: '2', label: 'B', w: 2, h: 1 },
    { id: '3', label: 'C', w: 3, h: 1 },
    { id: '4', label: 'D', w: 4, h: 1 },
]

let draggedShip = null
let draggedGrabbedIndex = null
let fromPalette = false

function onDragStart(item, fromIndex, ev) {
    draggedShip = item
    draggedGrabbedIndex = 0
    fromPalette = true
    ev.dataTransfer.setData('text/plain', JSON.stringify({
        shipId: item.id,
        grabbedIndex: 0
    }))
}

function onDrop(ev, idx) {
    const { shipId, grabbedIndex } = JSON.parse(ev.dataTransfer.getData('text/plain'))
    const row = Math.floor(idx / gridSize)
    const col = idx % gridSize

    // Если drag из палитры
    let ship = ships.value.find(s => s.id === shipId)
    if (!ship && fromPalette) {
        const base = palette.find(p => p.id === shipId)
        if (!base) return
        ship = {
            id: base.id,
            label: base.label,
            w: base.w,
            h: base.h,
            coords: []
        }
        ships.value.push(ship)
    }

    // Пересчитываем coords
    const newCoords = []
    for (let i = 0; i < ship.w; i++) {
        newCoords.push([col - grabbedIndex + i, row])
    }

    // Проверка границ
    if (newCoords.some(([x, y]) => x < 0 || x >= gridSize || y < 0 || y >= gridSize)) {
        return
    }

    ship.coords = newCoords

    draggedShip = null
    draggedGrabbedIndex = null
    fromPalette = false
}
</script>


<style scoped>
.grid {
    position: relative;
    display: grid;
    grid-template-columns: repeat(10, 40px);
    grid-template-rows: repeat(10, 40px);
    gap: 2px;
    width: fit-content;
}

.cell {
    width: 40px;
    height: 40px;
    background: #f5f5f5;
    border: 1px solid #ccc;
}

.palette {
    margin-top: 16px;
    display: flex;
    gap: 8px;
}

.palette-item {
    padding: 4px 8px;
    border: 1px solid #333;
    background: #ddd;
    cursor: grab;
}
</style>
