<!-- Ship.vue -->
<template>
    <div class="ship" :draggable="true" @dragstart="onDragStart">
        <div v-for="(coord, index) in coords" :key="index" class="cell" :style="{
            width: '42px',
            height: '42px',
            background: '#4caf50',
            color: '#fff',
            border: '0px solid #333',
            boxSizing: 'border-box',
            position: 'absolute',
            top: coord[1] * 42 + 'px',
            left: coord[0] * 42 + 'px',
        }">
            {{ label }}
        </div>
    </div>
</template>

<script setup>
// В mounted() или setup()
document.addEventListener('selectstart', e => {
    // если перетаскиваем или находимся над .grid — блокируем
    if (e.target.closest('.grid') || e.target.closest('.ship-placeholder')) {
        e.preventDefault();
    }
});

const props = defineProps({
    id: String,
    label: String,
    coords: Array,        // [[x,y],...]
    grabbedIndex: Number, // Клетка, за которую схватили (можно null)
})

function onDragStart(e) {
    e.dataTransfer.effectAllowed = 'move'
    e.dataTransfer.setData('text/plain', JSON.stringify({
        shipId: props.id,
        grabbedIndex: props.grabbedIndex ?? 0
    }))
}
</script>

<style scoped>
.ship {
    position: absolute;
}

.cell {
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>