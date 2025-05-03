<template>
    <div>
        <input type="text" v-model="roomId" placeholder="Введите ID комнаты" />
        <button @click="joinRoom">Присоединиться</button>
    </div>
</template>

<script setup>
import { ref } from 'vue';

const roomId = ref('');

function joinRoom() {
    console.log('Присоединение к комнате:', roomId.value);
}
</script>