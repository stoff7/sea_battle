<template>
    <div :class="['chat-message', isMine ? 'mine' : '']">
        <div class="bubble" :title="fullTime">
            <span class="author" v-if="!isMine">{{ message.author }}</span>
            <span class="text">{{ message.text }}</span>
            <span class="time">{{ shortTime }}</span>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue';
const props = defineProps({
    message: Object,
    currentUser: String
});
const isMine = computed(() => props.message.author === props.currentUser);
const date = computed(() => new Date(props.message.time));
const shortTime = computed(() =>
    date.value.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
);
const fullTime = computed(() => date.value.toLocaleString());
</script>

<style scoped>
.chat-message {
    display: flex;
    margin: 8px 0;
    animation: fadeIn 0.4s;
}

.mine {
    justify-content: flex-end;
}

.bubble {
    background: linear-gradient(120deg, #3498db 60%, #217dbb 100%);
    color: #fff;
    border-radius: 18px 18px 6px 18px;
    padding: 10px 18px 8px 14px;
    max-width: 70vw;
    min-width: 60px;
    box-shadow: 0 2px 12px #217dbb22;
    position: relative;
    font-size: 1.08em;
    transition: background 0.2s;
    word-break: break-word;
}

.mine .bubble {
    background: linear-gradient(120deg, #6dd5fa 60%, #3498db 100%);
    color: #1a2b3a;
    border-radius: 18px 18px 18px 6px;
}

.author {
    font-weight: 600;
    font-size: 0.97em;
    margin-right: 8px;
    color: #ffe082;
}

.time {
    font-size: 0.85em;
    color: #e3f2fd;
    margin-left: 12px;
    opacity: 0.7;
    float: right;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>