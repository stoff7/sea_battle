<template>
    <img :src="frames[currentFrame]" class="burning-fire"
        :style="{ width: size + 'px', height: size + 'px', pointerEvents: 'none', position: 'absolute', top: 0, left: 0 }"
        alt="fire" draggable="false" />
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';

const props = defineProps({
    size: { type: Number, default: 40 }, // размер клетки
    frames: { // массив путей к png/jpg/svg с огнем
        type: Array,
        required: true
    },
    speed: { type: Number, default: 80 } // скорость смены кадров (мс)
});

const currentFrame = ref(0);
let interval = null;

onMounted(() => {
    interval = setInterval(() => {
        currentFrame.value = (currentFrame.value + 1) % props.frames.length;
    }, props.speed);
});

onUnmounted(() => {
    clearInterval(interval);
});

watch(() => props.frames, () => {
    currentFrame.value = 0;
});
</script>

<style scoped>
.burning-fire {
    z-index: 20;
    pointer-events: none;
    user-select: none;
    /* Можно добавить glow: */
    filter: drop-shadow(0 0 8px #ff9800cc) brightness(1.2);
}
</style>