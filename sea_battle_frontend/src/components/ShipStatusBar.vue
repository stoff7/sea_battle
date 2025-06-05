<template>
    <div class="ship-status-bar">
        <div v-for="(ship, i) in sortedShips" :key="i" class="ship-status"
            :style="{ filter: ship.sunk ? 'brightness(0.5) sepia(1) hue-rotate(-30deg) saturate(4) opacity(0.7)' : '', background: ship.sunk ? 'rgba(120,0,0,0.45)' : '' }">
            <img :src="getShipImg({ ...ship, direction: 'vertical' })" :alt="`${ship.size}-deck`" :style="{
                width: cellPx + 'px',
                height: (cellPx * ship.size) + 'px',
                objectFit: 'cover',
                opacity: ship.sunk ? 0.7 : 1,
                borderRadius: '7px',
                boxShadow: ship.sunk ? '0 0 12px 2px #b71c1c' : '0 1px 4px #1976d2aa'
            }" />
        </div>
    </div>
</template>

<script>
const shipImages = import.meta.glob('@/assets/ships/*.png', { eager: true, import: 'default' });

export default {
    name: 'ShipStatusBar',
    props: {
        ships: { type: Array, required: true },
        cellPx: { type: Number, default: 40 }
    },
    computed: {
        sortedShips() {
            // Сортировка от 4 к 1 палубе
            return [...this.ships].sort((a, b) => b.size - a.size);
        }
    },
    methods: {
        getShipImg(ship) {
            const length = ship.size || Math.max(ship.w || 1, ship.h || 1);
            // direction всегда vertical
            return shipImages[`/src/assets/ships/ship_${length}x1_v.png`];
        }
    }
};
</script>

<style scoped>
.ship-status-bar {
    display: flex;
    gap: 12px;
    justify-content: center;
    align-items: flex-end;
    margin-top: 10px;
    flex-wrap: wrap;
}

.ship-status {
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 7px;
    padding: 2px;
    background: transparent;
    transition: filter 0.3s, background 0.3s;
}
</style>