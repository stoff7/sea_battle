
import { defineStore } from 'pinia'

export const useUsersStore = defineStore('users', {
  state: () => ({
    playerId: null,
    username: '',
    opponentId: null,
    opponentName: '',
    role:'',
  }),
  actions: {
    setPlayerId(id) {
      this.playerId = id
    },
    setUsername(name) {
      this.username = name
    },
    setOpponentId(id) {
      this.opponentId = id
    },
    setOpponentName(name) {
      this.opponentName = name
    },
    setRole(role) {
      this.role = role
    },
    reset() {
      this.playerId = null
      this.username = ''
      this.opponentId = null
      this.opponentName = ''
    }
  }
})