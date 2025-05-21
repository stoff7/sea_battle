<template>
  <div class="battle-container">

    <!-- Игровая сетка -->
    <div class="grid" @dragover.prevent @dragenter.prevent>
      <div v-for="idx in gridSize * gridSize" :key="idx" class="cell" :class="{
        hit: hits && hits.includes(idx - 1),
        miss: misses && misses.includes(idx - 1),
        'ship-hit-cell': isShipHitCell(idx - 1)
      }">
      </div>


      <!-- Отрисовка уже размещённых кораблей -->
      <div v-for="ship in ships" :key="ship.id" class="ship" :style="shipStyle(ship)">
        <div v-for="(coord, i) in ship.coords" :key="i" class="cell ship-cell"
          :class="{ hit: hits && hits.includes(coord[1] * gridSize + coord[0]) }" :data-index="i" :style="{
            width: cellPx + 'px',
            height: cellPx + 'px',
            position: 'absolute',
            top: (coord[1] - ship.coords[0][1]) * cellPx + 'px',
            left: (coord[0] - ship.coords[0][0]) * cellPx + 'px'
          }">
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
    misses: { type: Array, required: false },
    hits: { type: Array, required: false },
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
    isShipHitCell(idx) {
      // Проверяем, есть ли корабль в этой клетке и есть ли попадание
      if (!this.hits) return false;
      for (const ship of this.ships) {
        for (const [x, y] of ship.coords) {
          if (y * this.gridSize + x === idx && this.hits.includes(idx)) {
            return true;
          }
        }
      }
      return false;
    },
    coordsEqual(a, b) {
      if (a.length !== b.length) return false;
      const setA = new Set(a.map(([x, y]) => `${x},${y}`));
      const setB = new Set(b.map(([x, y]) => `${x},${y}`));
      if (setA.size !== setB.size) return false;
      for (const coord of setA) {
        if (!setB.has(coord)) return false;
      }
      return true;
    },

    rotateShip(ship) {
      const grabbedIndex = this.grabbedIndex ?? 0;
      const length = ship.coords.length;
      const [cx, cy] = ship.coords[grabbedIndex];
      const grabbedCoord = ship.coords[grabbedIndex];

      // +90°
      let newDirection = ship.direction === 'horizontal' ? 'vertical' : 'horizontal';
      let newCoords = [];
      if (newDirection === 'horizontal') {
        for (let i = 0; i < length; i++) {
          newCoords.push([cx - grabbedIndex + i, cy]);
        }
        // сортировка по x
        newCoords.sort((a, b) => a[0] - b[0]);
      } else {
        for (let i = 0; i < length; i++) {
          newCoords.push([cx, cy - grabbedIndex + i]);
        }
        // сортировка по y
        newCoords.sort((a, b) => a[1] - b[1]);
      }
      if (this.boundCheck(newCoords, ship.id) && !this.coordsEqual(ship.coords, newCoords)) {
        this.$emit('rotate-ship', {
          ...ship,
          direction: newDirection,
          w: newDirection === 'horizontal' ? length : 1,
          h: newDirection === 'vertical' ? length : 1,
          coords: newCoords
        });
        this.updateAdjacentHighlight();
        return;
      }

      // -90°
      newCoords = [];
      if (newDirection === 'horizontal') {
        for (let i = 0; i < length; i++) {
          newCoords.push([cx - grabbedIndex + (length - 1 - i), cy]);
        }
        newCoords.sort((a, b) => a[0] - b[0]);
      } else {
        for (let i = 0; i < length; i++) {
          newCoords.push([cx, cy - grabbedIndex + (length - 1 - i)]);
        }
        newCoords.sort((a, b) => a[1] - b[1]);
      }
      if (this.boundCheck(newCoords, ship.id) && !this.coordsEqual(ship.coords, newCoords)) {
        this.$emit('rotate-ship', {
          ...ship,
          direction: newDirection,
          w: newDirection === 'horizontal' ? length : 1,
          h: newDirection === 'vertical' ? length : 1,
          coords: newCoords
        });
        this.updateAdjacentHighlight();
        return;
      }

      // 180°
      newDirection = ship.direction;
      newCoords = [];
      if (newDirection === 'horizontal') {
        for (let i = 0; i < length; i++) {
          newCoords.push([cx - (i - grabbedIndex), cy]);
        }
        newCoords.sort((a, b) => a[0] - b[0]);
      } else {
        for (let i = 0; i < length; i++) {
          newCoords.push([cx, cy - (i - grabbedIndex)]);
        }
        newCoords.sort((a, b) => a[1] - b[1]);
      }
      if (this.boundCheck(newCoords, ship.id) && !this.coordsEqual(ship.coords, newCoords)) {
        this.$emit('rotate-ship', {
          ...ship,
          direction: newDirection,
          w: newDirection === 'horizontal' ? length : 1,
          h: newDirection === 'vertical' ? length : 1,
          coords: newCoords
        });
        this.updateAdjacentHighlight();
        return;
      }

      // Если ни один вариант не подошёл
      console.warn('Поворот невозможен для корабля', ship);
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
      let newCoords = [];
      if (ship.direction === 'horizontal') {
        const startX = col - grabbedIndex;
        const startY = row;
        for (let i = 0; i < ship.w; i++) {
          newCoords.push([startX + i, startY]);
        }
      } else {
        const startX = col;
        const startY = row - grabbedIndex;
        for (let i = 0; i < ship.h; i++) {
          newCoords.push([startX, startY + i]);
        }
      }
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
        // Определяем длину корабля
        const length = Math.max(payload.ship.w, payload.ship.h);
        let newCoords = [];
        if (payload.ship.direction === 'horizontal') {
          for (let i = 0; i < length; i++) {
            newCoords.push([col - payload.grabbedIndex + i, row]);
          }
        } else {
          for (let i = 0; i < length; i++) {
            newCoords.push([col, row - payload.grabbedIndex + i]);
          }
        }
        if (!this.boundCheck(newCoords, null)) {
          return;
        }
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
  position: absolute;
}

.ship-cell.hit {
  background: radial-gradient(circle, #e53935 60%, #b71c1c 100%) !important;
  color: #fff;
  box-shadow: 0 0 8px #e53935cc;
  animation: splash-hit 0.4s;
}

.ship-cell {
  /* background: #4c9baf;  <-- убрать отсюда */
  z-index: 10;
  color: white;
  border: 1px solid #333;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}

.ship-cell:not(.hit) {
  background: #4c9baf;
}

.ship-cell:hover {
  background: #3e7a8e;
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

.cell.hit {
  background: radial-gradient(circle, #e53935 60%, #b71c1c 100%);
  color: #fff;
  box-shadow: 0 0 8px #e53935cc;
  animation: splash-hit 0.4s;
}

.cell.miss {
  background: repeating-linear-gradient(135deg,
      #b0bec5 0px,
      #b0bec5 10px,
      #cfd8dc 10px,
      #cfd8dc 20px);
  color: #607d8b;
  animation: splash-miss 0.4s;
}

@keyframes splash-hit {
  0% {
    box-shadow: 0 0 0 #ff525200;
  }

  60% {
    box-shadow: 0 0 32px #ff5252cc;
  }

  100% {
    box-shadow: 0 0 16px 4px #ff5252cc;
  }
}
</style>
