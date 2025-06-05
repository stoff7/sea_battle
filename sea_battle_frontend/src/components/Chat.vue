<template>
    <div class="chat-container">
        <div class="chat-header">{{ $t('chat.chat') }}</div>
        <div class="chat-messages" ref="messagesBox">
            <div v-for="(msg, i) in messages" :key="i"
                :class="['chat-message', { 'mine': msg.fromPlayer === username }]">
                <span class="author">{{ msg.fromPlayer }}</span>
                <span class="text">{{ msg.text }}</span>
                <span class="time">{{ formatTime(msg.timestamp) }}</span>
            </div>
        </div>
        <form class="chat-input-row" @submit.prevent="handleSend">
            <input v-model="text" class="chat-input" :placeholder="$t('chat.message')" maxlength="200"
                autocomplete="off" />
            <button class="send-btn" :disabled="!text.trim()">➤</button>
        </form>
    </div>
</template>

<script>
import { ref, watch, nextTick } from 'vue';

export default {
    name: 'Chat',
    props: {
        gameId: String,
        playerId: [String, Number],
        username: String,
        ws: Object,
        messages: Array
    },
    data() {
        return {
            text: '',
        };
    },
    mounted() {
        this.$watch(
            () => this.messages.length,
            async () => {
                await nextTick();
                if (this.$refs.messagesBox) {
                    this.$refs.messagesBox.scrollTop = this.$refs.messagesBox.scrollHeight;
                }
            }
        );
    },
    methods: {
        handleSend() {
            if (!this.text.trim()) return;
            this.$emit('send', this.text);
            this.text = '';
        },
        formatTime(ts) {
            if (!ts) return '';
            const d = new Date(ts);
            return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        },

    }
};
</script>

<style scoped>
.chat-container {
    width: 100%;
    min-width: 0;
    max-width: 420px;
    background: #1a2636;
    border-radius: 14px;
    box-shadow: 0 4px 24px #217dbb22;
    display: flex;
    flex-direction: column;
    height: 100%;
}

.chat-header {
    background: linear-gradient(90deg, #3498db 0%, #217dbb 100%);
    color: #fff;
    font-weight: 700;
    font-size: 1.1em;
    padding: 10px 18px;
    border-radius: 14px 14px 0 0;
}

.chat-messages {
    flex: 1 1 auto;
    overflow-y: auto;
    padding: 12px;
    background: #22334a;
    min-height: 0;
}

.chat-message {
    margin-bottom: 10px;
    color: #e3f2fd;
    animation: fadeIn 0.3s;
    word-break: break-word;
}

.chat-message.mine {
    text-align: right;
    color: #90caf9;
}

.author {
    font-weight: 600;
    margin-right: 8px;
}

.text {
    margin-right: 8px;
}

.time {
    font-size: 0.9em;
    color: #b3e5fc;
    opacity: 0.7;
}

.chat-input-row {
    display: flex;
    align-items: center;
    padding: 10px;
    border-top: 1.5px solid #3498db33;
    background: #1a2636;
    border-radius: 0 0 14px 14px;
    gap: 10px;
    min-width: 0;
}

.chat-input {
    flex: 1 1 0;
    min-width: 0;
    padding: 12px 14px;
    border-radius: 8px;
    border: 1.5px solid #3498db;
    font-size: 1em;
    background: #22334a;
    color: #fff;
    outline: none;
    transition: border-color 0.2s, background 0.2s;
    box-sizing: border-box;
}

.chat-input:focus {
    border-color: #6dd5fa;
    background: #22334a;
}

.send-btn {
    flex: 0 0 auto;
    background: #3498db;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 1.3em;
    padding: 0 18px;
    cursor: pointer;
    transition: background 0.2s;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.send-btn:disabled {
    background: #b0bec5;
    cursor: not-allowed;
}

@media (max-width: 900px) {
    .chat-container {
        margin-left: 0;
        margin-top: 18px;
        width: 100%;
        max-width: 100vw;
        min-width: 0;
        height: 320px;
    }

    .chat-header {
        font-size: 1em;
        padding: 8px 10px;
    }

    .chat-input-row {
        padding: 8px;
        gap: 6px;
    }

    .chat-input {
        font-size: 0.98em;
        padding: 10px 8px;
    }

    .send-btn {
        font-size: 1.1em;
        padding: 0 10px;
        height: 38px;
    }
}
</style>