<template>
  <div class="grid" @dragover.prevent>
    <div
      v-for="(cell, index) in grid"
      :key="index"
      class="cell"
      @dragenter.prevent
      @dragover.prevent
      @drop="onDrop($event, index)"
    >
      <!-- Делает placeholder draggable -->
      <div
        v-if="cell"
        class="draggable-placeholder"
        draggable="true"
        @dragstart="onDragStart(cell, index, $event)"
      >
        {{ cell }}
      </div>
    </div>
  </div>

  <div class="palette">
    <div
      v-for="it in items"
      :key="it.id"
      class="palette-item"
      draggable="true"
      @dragstart="onDragStart(it, null, $event)"
    >
      {{ it.label }} ({{ it.w }}×{{ it.h }})
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// размер поля и плоский массив 100 ячеек :contentReference[oaicite:3]{index=3}
const gridSize = 10
const grid = ref(Array(gridSize * gridSize).fill(null))

// описание кораблей (w×h) :contentReference[oaicite:4]{index=4}
const items = [
  { id: 1, label: 'A', w: 1, h: 1 },
  { id: 2, label: 'B', w: 2, h: 1 },
  { id: 3, label: 'C', w: 3, h: 1 },
  { id: 4, label: 'D', w: 4, h: 1 },
]

let draggedItem = null
let draggedFromIndex = null

function onDragStart(item, fromIndex, ev) {
  draggedItem = item
  draggedFromIndex = fromIndex
  // разрешаем move-операцию :contentReference[oaicite:5]{index=5}
  ev.dataTransfer.effectAllowed = 'move'
  ev.dataTransfer.dropEffect = 'move'  // курсор “move” :contentReference[oaicite:6]{index=6}
}

function onDrop(ev, idx) {
  if (!draggedItem) return

  const row = Math.floor(idx / gridSize)
  const col = idx % gridSize

  // если из палитры — проверяем, что такого label ещё нет на поле :contentReference[oaicite:7]{index=7}
  const exists = grid.value.includes(draggedItem.label)
  if (draggedFromIndex === null && exists) {
    draggedItem = null
    return
  }

  // если перемещаем с поля — сначала очищаем старую область :contentReference[oaicite:8]{index=8}
  if (draggedFromIndex !== null) {
    const r0 = Math.floor(draggedFromIndex / gridSize)
    const c0 = draggedFromIndex % gridSize
    for (let dy = 0; dy < draggedItem.h; dy++) {
      for (let dx = 0; dx < draggedItem.w; dx++) {
        const r = r0 + dy, c = c0 + dx
        if (r < gridSize && c < gridSize) {
          grid.value[r * gridSize + c] = null
        }
      }
    }
  }

  // заполняем новую область w×h :contentReference[oaicite:9]{index=9}
  for (let dy = 0; dy < draggedItem.h; dy++) {
    for (let dx = 0; dx < draggedItem.w; dx++) {
      const r = row + dy, c = col + dx
      if (r < gridSize && c < gridSize) {
        grid.value[r * gridSize + c] = draggedItem.label
      }
    }
  }

  // сброс состояния перетаскивания
  draggedItem = null
  draggedFromIndex = null
}
</script>

<style scoped>
.grid {
  display: grid; 
  grid-template-columns: repeat(10, 40px);
  grid-template-rows:    repeat(10, 40px);
  gap: 2px;
}
.cell {
  border: 1px solid #ccc;
  background: #f5f5f5;
  position: relative;
}
.draggable-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: #4caf50; color: white;
  pointer-events: auto; 
}
.palette {
  margin-top: 16px; display: flex; gap: 8px;
}
.palette-item {
  padding: 4px 8px; border: 1px solid #333; cursor: grab;
}
</style>
