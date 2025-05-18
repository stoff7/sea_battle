<template>
  <div class="home">
    <h1>Главная</h1>
    <h1>Добро пожаловать в игру "Морской бой"</h1>
    <p>Играйте против компьютера или с другом!</p>
    <div class="game-options">
      <button @click="createRoom">Создать комнату</button>
      <div style="margin-top: 20px;">
        <p>Или присоединитесь по ID комнаты!</p>
        <input v-model="this.joinGameId" placeholder="Введите ID комнаты"
          style="padding: 10px; border: 1px solid #ccc; border-radius: 5px; width: 180px; margin-right: 10px;" />
        <button @click="joinRoom">Присоединиться</button>
      </div>
      <div class="user-input">
        <input v-model="username" @debounced-input="saveUsername" placeholder="Введите имя пользователя" />
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
export default {
  name: "HomeView",
  data() {
    return {
      api: 'https://' + import.meta.env.VITE_API + '/api/v1',
      playerId: null,
      gameId: null,
      username: '',
      joinGameId: null,
    };
  },
  created() {
    const storedUsername = localStorage.getItem('username');
    if (storedUsername) {
      this.username = storedUsername;
    }
  },
  methods: {
    async createRoom() {
      // const response = await axios.post(this.api + '/start_game', {
      //   userName: this.username,
      // });

      this.playerId = 1;
      this.gameId = 1;


      //console.log(response.data);
      // this.playerId = response.data.playerId;
      localStorage.setItem('playerId', this.playerId);
      console.log("ID игрока:", this.playerId);
      // this.gameId = response.data.gameId;
      console.log("ID игры:", this.gameId);
      console.log("Комната создана c ID:", this.gameId);
      console.log("Это ID игрока:", this.playerId);
      this.$router.push({ name: 'room', params: { gameId: this.gameId } });
    },
    saveUsername() {
      localStorage.setItem('username', this.username);
      console.log("Имя пользователя сохранено:", this.username);
    },
    async joinRoom() {
      if (!this.joinGameId) {
        alert("Пожалуйста, введите ID комнаты!");
        return;
      }
      this.gameId = this.joinGameId;
      const response = await axios.post(this.api + `/join_game`, {
        gameId: this.joinGameId,
        userName: this.username
      });
      this.playerId = response.data.playerId;
      localStorage.setItem('playerId', this.playerId);
      this.gameId = response.data.gameId;
      console.log("Присоединение к комнате c ID:", this.gameId);
      this.$router.push({ name: 'room', params: { playerId: this.playerId, gameId: this.gameId } });
    },
  }
};
</script>

<style scoped>
.home {
  text-align: center;
  margin-top: 40px;
  font-family: 'Segoe UI', Arial, sans-serif;
  background: #f4f8fb;
  min-height: 100vh;
  padding-bottom: 40px;
}

h1 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 700;
}

p {
  color: #555;
  font-size: 1.1em;
}

.game-options {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(44, 62, 80, 0.08);
  padding: 32px 24px;
  margin: 32px auto 0 auto;
  max-width: 420px;
  gap: 24px;
}

.game-options button {
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 12px 28px;
  font-size: 1em;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.08);
}

.game-options button:hover {
  background: #217dbb;
}

.game-options input {
  padding: 10px;
  border: 1px solid #bfc9d1;
  border-radius: 6px;
  width: 180px;
  margin-right: 10px;
  font-size: 1em;
  transition: border-color 0.2s;
}

.game-options input:focus {
  border-color: #3498db;
  outline: none;
}

.user-input {
  margin-top: 10px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.user-input input {
  padding: 10px;
  border: 1px solid #bfc9d1;
  border-radius: 6px;
  width: 250px;
  font-size: 1em;
  transition: border-color 0.2s;
}

.user-input input:focus {
  border-color: #3498db;
  outline: none;
}

@media (max-width: 600px) {
  .game-options {
    padding: 18px 8px;
    max-width: 98vw;
  }

  .user-input input,
  .game-options input {
    width: 98vw;
    max-width: 98vw;
  }
}

h1 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 700;
  font-size: 2.2em;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(44, 62, 80, 0.08);
  background: linear-gradient(90deg, #3498db 0%, #6dd5fa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

h1+h1 {
  font-size: 1.5em;
  margin-top: 0;
  margin-bottom: 18px;
  font-weight: 600;
  color: #217dbb;
  text-shadow: 0 1px 4px rgba(52, 152, 219, 0.10);
  background: none;
  -webkit-text-fill-color: initial;
}
</style>
