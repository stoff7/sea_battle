<template>
    <button class="lang-btn" @click="toggleLang" :title="nextLang.toUpperCase()">
        <img :src="langImg" :alt="nextLang" />
    </button>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import ruImg from '@/assets/language/ru.png';
import enImg from '@/assets/language/en.png';

const { locale } = useI18n();

const nextLang = computed(() => (locale.value === 'ru' ? 'en' : 'ru'));
const langImg = computed(() => nextLang.value === 'ru' ? enImg : ruImg);

function toggleLang() {
    locale.value = nextLang.value;
    localStorage.setItem('lang', locale.value);
}
</script>

<style scoped>
.lang-btn {
    width: 55px;
    height: 55px;
    border-radius: 50%;
    border: none;
    background: #22334a;
    box-shadow: 0 2px 8px #1976d2aa;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    margin-left: 18px;
    transition: box-shadow 0.2s, background 0.2s;
    padding: 0;
}

.lang-btn:hover {
    background: #1976d2;
    box-shadow: 0 4px 16px #1976d2cc;
}

.lang-btn img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 50%;
    pointer-events: none;
}
</style>