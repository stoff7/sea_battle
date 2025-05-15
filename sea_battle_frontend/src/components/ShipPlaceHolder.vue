<template>
  <div class="ship-placeholder" :draggable="true" @dragstart="onDragStart"
    :style="{ width: totalWidth + 'px', height: totalHeight + 'px' }">
    <!-- Строим сетку из ячеек -->
    <div v-for="row in rows" :key="`r${row}`" class="row">
      <div v-for="col in cols" :key="`r${row}c${col}`" class="cell"
        :style="{ width: cellSize + 'px', height: cellSize + 'px' }">
        {{ name }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue'

// Размер одной клетки в пикселях — вынесем в константу
const cellSize = 40

const cols = computed(() => Array.from({ length: props.width }, (_, i) => i))
const rows = computed(() => Array.from({ length: props.height }, (_, i) => i))

const totalWidth = computed(() => props.width * cellSize)
const totalHeight = computed(() => props.height * cellSize)

function onDragStart(e) {
  const payload = {
    id: props.id,
    name: props.name,
    width: props.width,
    height: props.height,
    length: props.length
  }
  e.dataTransfer.effectAllowed = 'move'
  e.dataTransfer.setData('application/json', JSON.stringify(payload))
}
</script>
<script>
export default {
  name: 'ShipPlaceHolder',
  props: {
    id: { type: Number, required: true },
    name: { type: String, required: true },
    width: { type: Number, required: true }, // в клетках
    height: { type: Number, required: true }, // в клетках
    length: { type: Number, required: true }, // можно убрать, если не нужен
  },
  data() {
    return {
      cellSize: 40,
    }
  },
}
</script>

<style scoped>
.ship-placeholder {
  display: inline-block;
  position: relative;
  cursor: grab;
  background: transparent;
}

/* ряды внутри плейсхолдера */
.row {
  display: flex;
}

/* каждая клетка */
.cell {
  box-sizing: border-box;
  border: 1px solid #999;
  background: #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}
</style>
