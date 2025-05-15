<template>
  <div class="home">
    <h1>Главная</h1>
    <h1>Добро пожаловать в игру "Морской бой"</h1>
    <p>Играйте против компьютера или с другом!</p>
    <div class="game-options">
      <button @click="createRoom">Создать комнату</button>
    </div>
    <div class="user-input">
      <input v-model="username" @debounced-input="saveUsername" placeholder="Введите имя пользователя" />
    </div>
  </div>
</template>

<script>
import axios from 'axios';
export default {
  name: "HomeView",
  data() {
    return {
      playerId: null,
      gameId: null,
      username: '',
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
      // const response = await axios.post('http://localhost:8077/api/v1/start_game', {
      //   userName: this.username,
      // });
      // playerId = response.data.playerId;
      // console.log("ID игрока:", playerId);
      // gameId = response.data.gameId;
      // console.log("ID игры:", gameId);

      this.gameId = Math.floor(Math.random() * 100000);
      console.log("Комната создана c ID:", this.gameId);
      this.$router.push({ name: 'room', params: { playerId: this.playerId, gameId: this.gameId } });
    },
    saveUsername() {
      localStorage.setItem('username', this.username);
      console.log("Имя пользователя сохранено:", this.username);
    }
  }
};
</script>

<style scoped>
.home {
  text-align: center;
  margin-top: 20px;
}

.game-options {
  display: flex;
  justify-content: space-between;
  margin: 20px auto;
  max-width: 400px;
}

.game-options p {
  flex: 1;
  text-align: center;
  margin: 0 10px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  cursor: pointer;
  background-color: #f9f9f9;
  transition: background-color 0.3s ease;
}

.game-options p:hover {
  background-color: #e0e0e0;
}

.user-input {
  margin-top: 20px;
}

.user-input input {
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  width: 250px;
  margin-right: 10px;
  transition: border-color 0.3s ease;
}

.user-input input:focus {
  outline: none;
  border-color: #66afe9;
}
</style>
