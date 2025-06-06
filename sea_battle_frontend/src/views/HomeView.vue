<template>
  <div class="home-page">
    <div class="header-container">
      <h1 class="military-title">{{ $t('home.title') }}</h1>
      <LanguageButton />
    </div>

    <!-- Фоновые изображения -->
    <div class="background-images">
      <div class="bg-img left" :class="{ active: hovered === 'join' }" :style="{ backgroundImage: `url(${leftImg})` }">
      </div>
      <div class="bg-img right" :class="{ active: hovered === 'create' }"
        :style="{ backgroundImage: `url(${rightImg})` }"></div>
    </div>
    <!-- Кликабельные контейнеры -->
    <div class="split-actions">
      <div class="split half join" @mouseenter="hovered = 'join'" @mouseleave="hovered = null" @click="showJoin = true">
        <span class="split-label" :class="{ active: hovered === 'join' }">{{ $t('home.join') }}</span>
      </div>
      <div class="split half create" @mouseenter="hovered = 'create'" @mouseleave="hovered = null"
        @click="showCreate = true">
        <span class="split-label" :class="{ active: hovered === 'create' }">{{ $t('home.create') }}</span>
      </div>
    </div>

    <!-- Модальное окно для присоединения -->
    <div v-if="showJoin" class="modal-overlay" @click.self="showJoin = false">
      <div class="modal">
        <h2>{{ $t('home.join_modal_title') }}</h2>
        <div class="user-input">
          <input id="username-input" v-model="username" v-debounce @debounced-input="saveUsername"
            :placeholder="$t('home.username_placeholder')" />
        </div>
        <input v-model="joinGameId" :placeholder="$t('home.room_id_placeholder')" />
        <div class="modal-actions">
          <button @click="joinRoom">{{ $t('home.join_by_id_btn') }}</button>
          <button @click="joinRandomRoom">{{ $t('home.join_random_btn') }}</button>
        </div>
        <button class="close-btn" @click="showJoin = false">{{ $t('home.modal_close') }}</button>
      </div>
    </div>

    <!-- Модальное окно для создания -->
    <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
      <div class="modal">
        <h2>{{ $t('home.create_modal_title') }}</h2>
        <div class="user-input">
          <input id="username-input" v-model="username" v-debounce @debounced-input="saveUsername"
            :placeholder="$t('home.username_placeholder')" />
        </div>
        <div class="modal-actions">
          <button @click="createRoom('OPEN')">{{ $t('home.create_open_btn') }}</button>
          <button @click="createRoom('CLOSE')">{{ $t('home.create_close_btn') }}</button>
        </div>
        <button class="close-btn" @click="showCreate = false">{{ $t('home.modal_close') }}</button>
      </div>
    </div>
  </div>
</template>

<script>
import { useUsersStore } from '@/stores/users';
import { useChatStore } from '@/stores/chat';
import leftImg from '@/assets/images/left.jpeg';
import rightImg from '@/assets/images/right.jpeg';
import LanguageButton from '@/components/LanguageButton.vue';
import { getApiFacade } from '@/logic/apiFacade';
import { createUser } from '@/logic/userFactory';

export default {
  name: "HomeView",
  components: { LanguageButton },
  data() {
    const usersStore = useUsersStore();
    const chatStore = useChatStore();
    chatStore.clear();
    const apiBase = 'https://' + import.meta.env.VITE_API + '/api/v1';
    return {
      apiFacade: getApiFacade(apiBase),
      usersStore,
      leftImg,
      rightImg,
      playerId: localStorage.getItem('playerId'),
      gameId: null,
      username: '',
      joinGameId: null,
      hostName: null,
      hostId: null,
      showJoin: false,
      showCreate: false,
      hovered: null,
    };
  },
  created() {
    const storedUsername = localStorage.getItem('username');
    if (storedUsername) {
      this.username = storedUsername;
      this.usersStore.setUsername(storedUsername);
    }
    localStorage.removeItem('gameId');
    localStorage.removeItem('playerId');
    localStorage.removeItem('roomState');
    localStorage.removeItem('opponentId');
    localStorage.removeItem('opponentName');
    localStorage.removeItem('role');
    localStorage.removeItem('hostId');
    localStorage.removeItem('hostName');
  },
  methods: {
    async createRoom(type) {
      if (!this.username) {
        alert("Пожалуйста, введите ваше имя пользователя!");
        return;
      }
      const data = await this.apiFacade.createRoom(this.username, type);
      const user = createUser({
        playerId: data.playerId,
        username: this.username,
        hostId: data.hostId,
        hostName: data.hostName,
        role: 'host'
      });
      this.playerId = user.playerId;
      this.gameId = data.gameId;
      this.usersStore.setUsername(user.username);
      this.usersStore.setPlayerId(user.playerId);
      this.usersStore.setOpponentId(user.hostId);
      this.usersStore.setOpponentName(user.hostName);
      this.usersStore.setRole(user.role);
      localStorage.removeItem('roomState');
      this.$router.push({ name: 'room', params: { gameId: this.gameId } });
    },
    saveUsername() {
      localStorage.setItem('username', this.username);
      this.usersStore.setUsername(this.username);
    },
    async joinRoom() {
      if (!this.joinGameId || !this.username) {
        alert("Пожалуйста, введите ID комнаты и имя пользователя!");
        return;
      }
      const data = await this.apiFacade.joinRoom(this.username, this.joinGameId);
      const user = createUser({
        playerId: data.playerId,
        username: this.username,
        hostId: data.hostId,
        hostName: data.hostName,
        role: 'guest'
      });
      this.playerId = user.playerId;
      this.gameId = data.gameId;
      this.usersStore.setUsername(user.username);
      this.usersStore.setPlayerId(user.playerId);
      this.usersStore.setOpponentId(user.hostId);
      this.usersStore.setOpponentName(user.hostName);
      this.usersStore.setRole(user.role);
      localStorage.removeItem('roomState');
      this.$router.push({ name: 'room', params: { gameId: this.gameId } });
    },
    async joinRandomRoom() {
      if (!this.username) {
        alert("Пожалуйста, введите ваше имя пользователя!");
        return;
      }
      const data = await this.apiFacade.joinRandomRoom(this.username);
      const role = data.hostName === null ? 'host' : 'guest';
      const user = createUser({
        playerId: data.playerId,
        username: this.username,
        hostId: data.hostId,
        hostName: data.hostName,
        role
      });
      this.playerId = user.playerId;
      this.gameId = data.gameId;
      this.usersStore.setUsername(user.username);
      this.usersStore.setPlayerId(user.playerId);
      this.usersStore.setOpponentId(user.hostId);
      this.usersStore.setOpponentName(user.hostName);
      this.usersStore.setRole(user.role);
      localStorage.removeItem('roomState');
      this.$router.push({ name: 'room', params: { gameId: this.gameId } });
    },
  },
};
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: #0f2027;
}

/* Фоновые изображения */
.background-images {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  z-index: 0;
  pointer-events: none;
}

.bg-img {
  flex: 1 1 0;
  background-size: cover;
  background-position: center;
  transition: transform 0.5s cubic-bezier(.4, 2, .3, 1), filter 0.4s;
  filter: brightness(0.7) blur(0.5px);
  will-change: transform, filter;
}

.bg-img.left.active,
.bg-img.right.active {
  transform: scale(1.03);
  filter: brightness(1) blur(0px);
  z-index: 1;
}

/* Кликабельные контейнеры */
.split-actions {
  flex: 1 1 auto;
  display: flex;
  width: 100vw;
  max-width: 100vw;
  margin: 0;
  height: 100%;
  min-height: 0;
  gap: 0;
  position: relative;
  z-index: 2;
  padding: 0;
}

.split.half {
  flex: 1 1 0;
  min-width: 0;
  min-height: 0;
  height: 100%;
  border-radius: 0;
  box-shadow: none;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: transparent;
  transition: background 0.2s;
}

.split-label {
  color: #fff;
  font-size: 2.5em;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px #1976d2cc, 0 1px 0 #217dbb;
  user-select: none;
  transition: transform 0.35s cubic-bezier(.4, 2, .3, 1), color 0.25s, text-shadow 0.25s;
  padding-bottom: 8vh;
  opacity: 0.92;
}

.split-label.active {
  transform: scale(1.15) translateY(-10px);
  color: #6dd5fa;
  text-shadow: 0 4px 24px #1976d2, 0 1px 0 #fff;
  opacity: 1;
}

.split.half:hover {
  background: rgba(15, 32, 39, 0.08);
}

.home-page::before {
  content: "";
  position: fixed;
  inset: 0;
  background: rgba(15, 32, 39, 0.82);
  z-index: 0;
  pointer-events: none;
}

.home-page>h1,
.home-page>.user-input {
  z-index: 2;
  position: relative;
  margin-top: 40px;
  margin-bottom: 18px;
  background: none;
}


.home-page p {
  color: #e3f2fd;
  font-size: 1.1em;
}

.home-page .user-input {
  margin: 0 auto 28px auto;
  width: 100%;
  max-width: 340px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.home-page .user-input input {
  padding: 12px;
  border: 1.5px solid #3b5b7a;
  border-radius: 8px;
  width: 250px;
  font-size: 1.1em;
  background: #22334a;
  color: #ffffff;
  transition: border-color 0.2s, background 0.2s;
}

.home-page .user-input input::placeholder {
  color: #b3e5fc;
  opacity: 1;
}

.home-page .user-input input:focus {
  border-color: #6dd5fa;
  background: #22334a;
  outline: none;
}

.home-page .split-actions {
  flex: 1 1 auto;
  display: flex;
  width: 100vw;
  max-width: 100vw;
  margin: 0;
  height: 100%;
  min-height: 0;
  gap: 0;
  position: relative;
  z-index: 2;
  padding: 0;
  /* ГЛАВНОЕ: пусть растёт */
  flex-grow: 1;
}

.home-page .split {
  position: relative;
}

.home-page .split.half {
  flex: 1 1 0;
  min-width: 0;
  min-height: 0;
  height: 100%;
  border-radius: 0;
  box-shadow: none;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: transparent;
  transition: background 0.2s;
}

.home-page .split.half:first-child {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.home-page .split.half:last-child {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.home-page .split .overlay {
  width: 100%;
  background: linear-gradient(0deg, rgba(15, 32, 39, 0.92) 60%, rgba(15, 32, 39, 0.2) 100%);
  padding: 32px 0 24px 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.home-page .split .overlay span {
  color: #fff;
  font-size: 2.5em;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px #1976d2cc, 0 1px 0 #217dbb;
  user-select: none;
}

.home-page .split.half:hover .overlay {
  background: linear-gradient(0deg, rgba(15, 32, 39, 0.98) 70%, rgba(15, 32, 39, 0.4) 100%);
}

.home-page .modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 32, 39, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.home-page .modal {
  background: #1a2636;
  border-radius: 16px;
  padding: 28px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 4px 32px #000a;
  position: relative;
  color: #ffffff;
}

.home-page .modal h2 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 1.5em;
  color: #90caf9;
  text-shadow: 0 2px 8px #000a;
}

.home-page .modal input {
  padding: 12px;
  border: 1.5px solid #3b5b7a;
  border-radius: 8px;
  width: 90%;
  margin-bottom: 16px;
  font-size: 1.1em;
  background: #22334a;
  color: #ffffff;
  transition: border-color 0.2s, background 0.2s;
}

.home-page .modal input::placeholder {
  color: #b3e5fc;
  opacity: 1;
}

.home-page .modal input:focus {
  border-color: #6dd5fa;
  background: #1a2636;
  outline: none;
}

.home-page .modal-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.home-page .modal-actions button {
  background: linear-gradient(120deg, #217dbb 0%, #3498db 100%);
  color: #ffffff;
  text-shadow: 0 1px 4px #000c;
  border: none;
  border-radius: 8px;
  padding: 12px 28px;
  font-size: 1.1em;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s;
  box-shadow: 0 2px 8px #000a;
  text-shadow: 0 1px 4px #000a;
}

.home-page .modal-actions button:hover {
  background: linear-gradient(120deg, #6dd5fa 0%, #217dbb 100%);
  color: #fff;
}

.home-page .close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  color: #6dd5fa;
  font-size: 1.5em;
  cursor: pointer;
  transition: color 0.2s;
}

.home-page .close-btn:hover {
  color: #fff;
}


.header-container {
  width: 100vw;
  max-width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 48px;
  padding-bottom: 12px;


  position: relative;
  z-index: 3;
}

.military-title {
  font-family: 'Impact', 'Arial Black', sans-serif;
  font-size: 106px;
  color: #e0d8cc;
  /* Костно-белый оттенок */
  letter-spacing: 12px;
  text-transform: uppercase;
  text-align: center;
  /* text-shadow:
    0 4px 12px #222a33,
    0 8px 32px #1976d2cc,
    0 2px 0 #217dbb; */
  font-weight: 900;
  margin: 0;
  padding: 0;
  line-height: 1.05;
  filter: drop-shadow(0 6px 24px #12325a);
  position: relative;
  z-index: 3;
  background: none;
  /* Без анимаций! */
}

@media (max-width: 900px) {
  .background-images {
    flex-direction: column;
  }

  .split-actions {
    flex-direction: column;
    height: 100%;
    max-width: 100vw;
  }

  .split.half {
    min-height: 30vh;
    height: 50%;
    border-radius: 0 !important;
    margin-bottom: 0;
  }

  .split-label {
    font-size: 1.3em;
    padding-bottom: 4vh;
  }
}

.user-input {
  margin: 0 auto 28px auto;
  width: 100%;
  max-width: 340px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 2;
  align-items: center;
}

/* .input-label {
  color: #fff;

  font-size: 1.18em;
  font-weight: 800;
  margin-bottom: 10px;
  letter-spacing: 1.2px;
  text-shadow:
    0 2px 12px #1976d2cc,
    0 1px 0 #217dbb,
    0 0 12px #1976d288;
  background: linear-gradient(90deg, #6dd5fa 10%, #1976d2 90%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  padding-left: 2px;
  text-align: center;
  border-radius: 0;
  filter: drop-shadow(0 2px 8px #12325a);
} */
</style>
