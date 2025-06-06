import axios from 'axios';

class ApiFacade {
  constructor(apiBase) {
    this.api = apiBase;
  }

  async createRoom(username, type) {
    const response = await axios.post(`${this.api}/start_game`, {
      userName: username,
      gameType: type,
    });

    return response.data;
  }

  async joinRoom(username, gameId) {
    const response = await axios.post(`${this.api}/join_game`, {
      userName: username,
      gameId,
    });
    return response.data;
  }

  async joinRandomRoom(username) {
    const response = await axios.post(`${this.api}/join_random_game`, {
      userName: username,
    });
    return response.data;
  }
    async leaveGame(api, gameId, playerId) {
    const response = await axios.post(`https://${api}/api/v1/${gameId}/leave_game`, {
      playerId,
    });
    return response.data;
  }

  async sendChatMessage(api, gameId, playerId, text) {
    const response = await axios.post(`https://${api}/api/v1/${gameId}/chat_message`, {
      playerId,
      textMessage: text,
    });
    return response.data;
  }

  async fight(api, gameId, playerId, coord) {
    const response = await axios.patch(`https://${api}/api/v1/${gameId}/fight`, {
      playerId,
      coord,
    });
    return response;
  }

  async replayGame(api, gameId, playerId) {
    console.log(`Replaying game ${gameId} for player ${playerId}`);
    const response = await axios.patch(`https://${api}/api/v1/${gameId}/replay_game`, {
      playerId,
    });
    return response.data;
  }
  
  async setReady(api, gameId, playerId, ready, ships) {
    const response = await axios.post(`https://${api}/api/v1/${gameId}/ready_game`, {
      playerId,
      ready,
      ships,
    });
    return response.data;
  }
}

// Singleton
let apiInstance = null;
export function getApiFacade(apiBase) {
  if (!apiInstance) {
    apiInstance = new ApiFacade(apiBase);
  }
  return apiInstance;
}

