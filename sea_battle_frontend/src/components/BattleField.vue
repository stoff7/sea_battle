<template>
  <div class="battle-container">

    <!-- Игровая сетка -->
    <div class="grid" :style="{
      gridTemplateColumns: `repeat(10, ${cellPx}px)`,
      gridTemplateRows: `repeat(10, ${cellPx}px)`
    }" @dragover.prevent @dragenter.prevent>
      <div v-for="idx in gridSize * gridSize" :key="idx" class="cell" :class="{
        hit: hits && hits.includes(idx - 1),
        miss: misses && misses.includes(idx - 1),
        'adjacent-cell': adjacentCells.has(idx - 1),
        'ship-cell': ships.some(ship => ship.coords.some(([x, y]) => y * gridSize + x === idx - 1))
      }" @dragover.prevent @drop="onDrop($event, idx - 1)" :style="{ width: cellPx + 'px', height: cellPx + 'px' }">
        <span v-if="hits && hits.includes(idx - 1)">✸</span>
        <span v-else-if="misses && misses.includes(idx - 1)">○</span>
      </div>

      <!-- Отрисовка уже размещённых кораблей -->
      <div v-for="ship in ships" :key="ship.id" class="ship" :style="shipStyle(ship)"
        @dblclick="!ready && removeShip(ship)" :draggable="!ready" @dragstart="!ready && onShipDragStart(ship, $event)"
        @click="!ready && rotateShip(ship, $event)" @pointerdown="!ready && onPointerDown($event, ship)">
        <img :src="getShipImage(ship)" :style="{
          width: ship.direction === 'horizontal' ? (cellPx * ship.w) + 'px' : cellPx + 'px',
          height: ship.direction === 'vertical' ? (cellPx * ship.h) + 'px' : cellPx + 'px',
          objectFit: 'cover',
          position: 'absolute',
          top: 0,
          left: 0,
          pointerEvents: 'none',
          transition: 'transform 0.2s'
        }" draggable="false" alt="" />
        <div v-for="(coord, i) in ship.coords" :key="'hit' + i"
          v-if="Array.isArray(coord) && coord.length === 2 && hits && hits.includes(coord[1] * gridSize + coord[0])"
          class="ship-hit-overlay" :style="{
            width: cellPx + 'px',
            height: cellPx + 'px',
            position: 'absolute',
            top: (coord[1] - Math.min(...ship.coords.filter(c => Array.isArray(c) && c.length === 2).map(([, y]) => y))) * cellPx + 'px',
            left: (coord[0] - Math.min(...ship.coords.filter(c => Array.isArray(c) && c.length === 2).map(([x]) => x))) * cellPx + 'px',
            background: 'rgba(229,57,53,0.5)',
            pointerEvents: 'none'
          }"></div>
      </div>
    </div>

    <!-- Список доступных кораблей -->
    <div class="palette" style="display: grid; grid-template-columns: repeat(2, max-content);">
      <div v-for="ship in availableShips" :key="ship.id" class="ship-placeholder" draggable="true"
        @pointerdown="onPointerDown($event, ship)"
        @dragstart="onDragStart({ ...ship, direction: 'horizontal', w: Math.max(ship.w, ship.h), h: 1, angle: 90 }, $event)"
        :style="{ width: (Math.max(ship.w, ship.h) * cellPx) + 'px', height: cellPx + 'px', position: 'relative' }">
        <img :src="getShipImage({ ...ship, w: Math.max(ship.w, ship.h), h: 1 })" :style="{
          width: (Math.max(ship.w, ship.h) * cellPx) + 'px',
          height: cellPx + 'px',
          objectFit: 'cover',
          position: 'absolute',
          top: 0,
          left: 0,
          pointerEvents: 'none',
          transition: 'transform 0.2s'
        }" draggable="false" alt="" />
      </div>
    </div>
  </div>
</template>

<script>
import Burning from './Burning.vue';

const shipImages = import.meta.glob('@/assets/ships/*.png', { eager: true, import: 'default' });

export default {
  name: 'BattleField',
  // components: { Burning },
  props: {
    availableShips: { type: Array, required: true },
    ships: { type: Array, required: true },
    ready: { type: Boolean, required: true },
    misses: { type: Array, required: false },
    hits: { type: Array, required: false },
    cellPx: { type: Number, default: 40 }
  },
  components: {
    Burning
  },
  data() {
    return {
      gridSize: 10,
      dragged: null,
      grabbedIndex: 0,
      adjacentCells: new Set(),
      myShips: JSON.parse(localStorage.getItem('myShips')) || [],
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

    rotateShip(ship, event) {
      if (this.ready) {
        return;
      }
      this.onPointerDown(event, ship);
      const grabbedIndex = this.grabbedIndex ?? 0;

      const length = ship.coords.length;
      const [cx, cy] = ship.coords[grabbedIndex];

      // Генерируем все три варианта поворота
      const variants = [
        // +90°
        () => {
          let newDirection = ship.direction === 'horizontal' ? 'vertical' : 'horizontal';
          let newCoords = [];
          if (newDirection === 'horizontal') {
            for (let i = 0; i < length; i++) newCoords.push([cx - grabbedIndex + i, cy]);
            newCoords.sort((a, b) => a[0] - b[0]);
          } else {
            for (let i = 0; i < length; i++) newCoords.push([cx, cy - grabbedIndex + i]);
            newCoords.sort((a, b) => a[1] - b[1]);
          }
          return {
            direction: newDirection,
            coords: newCoords,
            w: newDirection === 'horizontal' ? length : 1,
            h: newDirection === 'vertical' ? length : 1,
          };
        },
        // -90°
        () => {
          let newDirection = ship.direction === 'horizontal' ? 'vertical' : 'horizontal';
          let newCoords = [];
          if (newDirection === 'horizontal') {
            for (let i = 0; i < length; i++) newCoords.push([cx - grabbedIndex + (length - 1 - i), cy]);
            newCoords.sort((a, b) => a[0] - b[0]);
          } else {
            for (let i = 0; i < length; i++) newCoords.push([cx, cy - grabbedIndex + (length - 1 - i)]);
            newCoords.sort((a, b) => a[1] - b[1]);
          }
          return {
            direction: newDirection,
            coords: newCoords,
            w: newDirection === 'horizontal' ? length : 1,
            h: newDirection === 'vertical' ? length : 1,
          };
        },
        // 180°
        () => {
          let newDirection = ship.direction;
          let newCoords = [];
          if (newDirection === 'horizontal') {
            for (let i = 0; i < length; i++) newCoords.push([cx - (i - grabbedIndex), cy]);
            newCoords.sort((a, b) => a[0] - b[0]);
          } else {
            for (let i = 0; i < length; i++) newCoords.push([cx, cy - (i - grabbedIndex)]);
            newCoords.sort((a, b) => a[1] - b[1]);
          }
          return {
            direction: newDirection,
            coords: newCoords,
            w: newDirection === 'horizontal' ? length : 1,
            h: newDirection === 'vertical' ? length : 1,
          };
        }
      ];

      // Перемешиваем варианты случайным образом
      for (let i = variants.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [variants[i], variants[j]] = [variants[j], variants[i]];
      }

      // Пробуем варианты по очереди
      for (const variantFn of variants) {
        const v = variantFn();
        if (
          this.boundCheck(v.coords, ship.id) &&
          !this.coordsEqual(ship.coords, v.coords)
        ) {
          this.$emit('rotate-ship', {
            ...ship,
            direction: v.direction,
            w: v.w,
            h: v.h,
            coords: v.coords
          });
          this.updateAdjacentHighlight();
          return;
        }
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
    onPointerDown(e, ship) {
      // Получаем координаты клика относительно корабля
      const rect = e.currentTarget.getBoundingClientRect();
      const x = Math.floor((e.clientX - rect.left) / this.cellPx);
      const y = Math.floor((e.clientY - rect.top) / this.cellPx);

      if (ship.direction === 'horizontal') {
        this.grabbedIndex = x;
      } else {
        this.grabbedIndex = y;
      }
    },
    onShipDragStart(ship, ev) {
      if (this.ready) {
        return;
      }
      this.onPointerDown(ev, ship);
      console.log('Dragging ship:', ship);
      console.log('Grabbed index:', this.grabbedIndex);


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
      this.onPointerDown(ev, ship);
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
        let direction = payload.ship.direction;
        let angle = payload.ship.angle ?? 90; // по умолчанию горизонтально

        if (direction === 'horizontal') {
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
          ship: {
            ...payload.ship,
            coords: newCoords,
            direction,
            angle,
            w: direction === 'horizontal' ? length : 1,
            h: direction === 'vertical' ? length : 1,
          },
          row, col,
          grabbedIndex: payload.grabbedIndex
        });
        this.updateAdjacentHighlight();
      }
      this.resetDragState();
    },
    shipStyle(ship) {
      // Найти минимальные x и y
      const minX = Math.min(...ship.coords.map(([x]) => x));
      const minY = Math.min(...ship.coords.map(([, y]) => y));
      const w = ship.direction === 'horizontal' ? ship.w : 1;
      const h = ship.direction === 'vertical' ? ship.h : 1;
      return {
        position: 'absolute',
        top: (minY * this.cellPx) + 'px',
        left: (minX * this.cellPx) + 'px',
        width: (w * this.cellPx) + 'px',
        height: (h * this.cellPx) + 'px',
        pointerEvents: 'auto',
        zIndex: 10,
      };
    },
    getAdjacentCells(ship) {
      const deltas = [
        [-1, -1], [-1, 0], [-1, 1],
        [0, -1], [0, 1],
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
    },
    getShipImage(ship) {
      const length = Math.max(ship.w, ship.h);
      if (ship.direction === 'horizontal') {
        return shipImages[`/src/assets/ships/ship_${length}x1_h.png`];
      } else {
        return shipImages[`/src/assets/ships/ship_${length}x1_v.png`];
      }
    },
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
  gap: 0px;
  background: #4c9baf;
}

.cell.ship-cell {
  background: rgba(118, 32, 32, 0.856);
}

.cell {
  background: #4c9baf !important;
  border: 0.01px solid #cccccc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ship {
  cursor: grab;
  position: absolute;
}

.ship-hit-overlay {
  z-index: 20;
  border-radius: 4px;
  animation: splash-hit 0.4s;
}

.ship-cell.hit {
  background: radial-gradient(circle, #e53935 60%, #b71c1c 100%) !important;
  color: #fff;
  box-shadow: 0 0 8px #e53935cc;
  animation: splash-hit 0.4s;
}

.ship-cell {
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
  /* Прозрачный фон у корабля, но перекроем inline-стилем */
  transition: background 0.2s;
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
  column-count: 2;
  column-gap: 8px;
}

.ship-placeholder {
  user-select: none;
  cursor: grab;
  background: transparent;
  break-inside: avoid;
  -webkit-column-break-inside: avoid;
  margin-bottom: 4px;
}

.adjacent-cell {
  background-color: #2f586261 !important;
}

.cell.hit {
  background: radial-gradient(circle, #e53935 60%, #b71c1c 100%);
  color: #fff;
  box-shadow: 0 0 8px #e53935cc;
  animation: splash-hit 0.4s;
}

.cell.miss {
  background: repeating-linear-gradient(135deg,
      #b0bec58b 0px,
      #b0bec582 10px,
      #cfd8dc89 10px,
      #cfd8dc7c 20px) !important;
  color: #607d8b !important;
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
