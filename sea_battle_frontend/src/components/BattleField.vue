<template>
  <div class="battle-container">

    <!-- Игровая сетка -->
    <div class="grid" @dragover.prevent @dragenter.prevent>
      <div v-for="(cell, idx) in gridSize * gridSize" :key="idx" class="cell"
        :class="{ 'adjacent-cell': adjacentCells.has(idx) }" @dragover.prevent @drop="onDrop($event, idx)">
      </div>


      <!-- Отрисовка уже размещённых кораблей -->
      <div v-for="ship in ships" :key="ship.id" class="ship" :style="shipStyle(ship)" @dblclick="removeShip(ship)"
        draggable="true" @pointerdown="onPointerDown($event)" @dragstart="onShipDragStart(ship, $event)">
        <div v-for="(coord, i) in ship.coords" :key="i" class="cell ship-cell" :data-index="i"
          :style="{ width: cellPx + 'px', height: cellPx + 'px' }">
          {{ ship.name }}
        </div>
      </div>
    </div>

    <!-- Список доступных кораблей -->
    <div class="palette" style="display: grid-template-columns repeat(2, max-content);">
      <div v-for="ship in availableShips" :key="ship.id" class="ship-placeholder" draggable="true"
        @pointerdown="onPointerDown($event)" @dragstart="onDragStart(ship, $event)"
        :style="{ width: ship.w * cellPx + 'px', height: ship.h * cellPx + 'px' }">
        <!-- Клеточная отрисовка плейсхолдера -->
        <div class="row" v-for="r in ship.h" :key="`r${r}`">
          <div v-for="c in ship.w" :key="`r${r}c${c}`" class="cell ship-cell" :data-index="c - 1"
            :style="{ width: cellPx + 'px', height: cellPx + 'px' }">
            {{ ship.name }}
          </div>
        </div>
      </div>
      <button @click="randomizeShips()" class="ship-placeholder" style="width: 100%; margin-top: 10px;">
        Случайное размещение
      </button>
    </div>
  </div>
</template>

<script>

export default {
  name: 'BattleField',
  props: {
    availableShips: { type: Array, required: true },
    ships: { type: Array, required: true },
    ready: { type: Boolean, required: true },
  },
  data() {
    return {
      gridSize: 10,
      cellPx: 40,
      dragged: null,
      grabbedIndex: 0,
      adjacentCells: new Set(),
    };
  },
  watch: {
    // Будет срабатывать на любую мутацию массива ships (добавление/удаление/изменение coords)
    ships: {
      handler() {
        this.updateAdjacentHighlight();
      },
      deep: true,
      immediate: true, // сразу при монтировании
    }
  },
  methods: {
    removeShip(ship) {
      this.$emit('remove-ship', ship);
    },

    canPlaceAt(cellIndices, ignoreShipId = null) {
      const forbidden = new Set();

      for (const ship of this.ships) {
        if (ship.id === ignoreShipId) continue;

        // занятые клетки
        ship.coords.forEach(([x, y]) =>
          forbidden.add(y * this.gridSize + x)
        );

        // соседние
        this.getAdjacentCells(ship).forEach(i =>
          forbidden.add(i)
        );
      }

      return cellIndices.every(i => !forbidden.has(i));
    },
    // Сохраняем индекс захваченной ячейки
    onPointerDown(e) {
      const cell = e.target.closest('.ship-cell');
      if (cell && cell.dataset.index !== undefined) {
        this.grabbedIndex = Number(cell.dataset.index);
      } else {
        // для плейсхолдера возьмем первую клетку
        this.grabbedIndex = 0;
      }
    },
    onShipDragStart(ship, ev) {
      if (this.ready) {
        return;
      }

      this.dragged = ship;
      ev.dataTransfer.effectAllowed = 'move';
      ev.dataTransfer.setData('application/json',
        JSON.stringify({
          shipId: ship.id,
          grabbedIndex: this.grabbedIndex
        })

      );
      this.updateAdjacentHighlight();
    },
    moveShip(ship, row, col, grabbedIndex) {
      const startX = col - grabbedIndex;
      const startY = row;
      const newCoords = [];
      for (let i = 0; i < ship.w; i++) {
        newCoords.push([startX + i, startY]);
      }
      //
      if (!this.boundCheck(newCoords, ship.id)) {
        return;
      }
      ship.coords = newCoords;
      this.updateAdjacentHighlight();
    },
    onDragStart(ship, ev) {
      this.dragged = ship;
      ev.dataTransfer.effectAllowed = 'move';
      ev.dataTransfer.setData('application/json',
        JSON.stringify({ ship, grabbedIndex: this.grabbedIndex })
      );
    },
    onDrop(ev, idx) {
      const payload = JSON.parse(ev.dataTransfer.getData('application/json'));
      const row = Math.floor(idx / this.gridSize);
      const col = idx % this.gridSize;

      const existing = this.ships.find(s => s.id === payload.shipId);
      if (existing) {
        this.moveShip(existing, row, col, payload.grabbedIndex);
      } else {
        // Генерим новые coords
        const newCoords = [];
        for (let i = 0; i < payload.ship.w; i++) {
          newCoords.push([col - payload.grabbedIndex + i, row]);
        }
        // Проверка, что каждая клетка внутри поля
        if (!this.boundCheck(newCoords, null)) {
          return;
        }

        // Если всё ок — эмитим создание
        this.$emit('place-ship', {
          ship: payload.ship,
          row, col,
          grabbedIndex: payload.grabbedIndex
        });
        this.updateAdjacentHighlight();
      }

      this.resetDragState();
    },
    shipStyle(ship) {
      const [x0, y0] = ship.coords[0];
      return {
        position: 'absolute',
        top: y0 * this.cellPx + 'px',
        left: x0 * this.cellPx + 'px',
        display: 'flex'
      };
    },
    getAdjacentCells(ship) {
      const deltas = [
        [-1, -1], [-1, 0], [-1, 1],
        [0, -1], /*self*/[0, 1],
        [1, -1], [1, 0], [1, 1],
      ];
      const neighbors = new Set();

      for (const [x, y] of ship.coords) {
        for (const [dx, dy] of deltas) {
          const nx = x + dx;
          const ny = y + dy;
          if (
            nx >= 0 && nx < this.gridSize &&
            ny >= 0 && ny < this.gridSize &&
            !ship.coords.some(([sx, sy]) => sx === nx && sy === ny)
          ) {
            neighbors.add(ny * this.gridSize + nx);
          }
        }
      }

      return neighbors;
    },
    updateAdjacentHighlight() {
      this.adjacentCells.clear();
      for (const ship of this.ships) {
        const adj = this.getAdjacentCells(ship);
        adj.forEach(i => this.adjacentCells.add(i));
      }
    },
    resetDragState() {
      this.dragged = null;
      this.grabbedIndex = 0;
    },
    boundCheck(newCoords, shipId) {
      const allInside = newCoords.every(
        ([x, y]) =>
          x >= 0 && x < this.gridSize &&
          y >= 0 && y < this.gridSize
      );
      if (!allInside) {
        console.warn('Нельзя ставить корабль за пределами поля:', newCoords);
        this.resetDragState();
        return false;
      }

      const flatIndices = newCoords.map(([x, y]) => y * this.gridSize + x);
      if (!this.canPlaceAt(flatIndices, shipId)) {
        console.warn('Нельзя ставить на занятые или соседние клетки');
        this.resetDragState();
        return false;
      }
      return true;
    }
  }
};
</script>

<style scoped>
.battle-container {
  display: flex;
  gap: 5px;
}

.grid {
  position: relative;
  display: grid;
  grid-template-columns: repeat(10, 40px);
  grid-template-rows: repeat(10, 40px);
  gap: 0px;
  background: #eef;
}

.cell {
  width: 40px;
  height: 40px;
  background: #fff;
  border: 1px solid #ccc;
}

.ship {
  cursor: grab;
}

.ship-cell {
  background: #4c9baf;
  color: white;
  border: 1px solid #333;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}

.ship-cell:hover {
  background: #3e7a8e93;
}

.ship-placeholder {
  cursor: grab;
  position: relative;
  user-select: none;
}

.row {
  display: flex;
}

.palette {
  /* Две колонки без grid-template-columns */
  column-count: 2;
  column-gap: 8px;
}

.ship-placeholder {
  user-select: none;
  cursor: grab;
  background: transparent;
  /* Не разрывать между колонками */
  break-inside: avoid;
  -webkit-column-break-inside: avoid;
  margin-bottom: 4px;
  /* уменьшенный gap между рядами */
}

.adjacent-cell {
  background-color: rgba(129, 128, 128, 0.036);
}

button.ship-placeholder {
  background: #307c8e;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 0;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 10px;
  margin-left: 10px;
  box-shadow: 0 2px 8px rgba(76, 155, 175, 0.08);
}

button.ship-placeholder:hover {
  background: #21525f;
}
</style>
