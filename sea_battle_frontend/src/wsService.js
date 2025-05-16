// src/services/wsService.js
import { Client } from '@stomp/stompjs'

// Singleton-сервис для STOMP-WebSocket
class WsService {
  constructor() {
    /** @type {Client|null} */
    this.client = null
    /** текущий gameId, к которому подключены */
    this.gameId = null
    /** накопленные подписки { topic, handler } */
    this.subscriptions = []
  }

  /**
   * Подключиться к WebSocket-каналу для конкретной игры.
   * @param {string} host — ваш ngrok-host без протокола, например "foo.ngrok.io"
   * @param {string} gameId
   */
  connect(host, gameId) {
    // если уже подключены к тому же gameId и активны — ничего не делаем
    if (this.client && this.client.active && this.gameId === gameId) {
      return
    }
    // иначе сбросим старый
    this.disconnect()

    this.gameId = gameId
    // собираем wss-URL
    const brokerURL = `wss://${host}/ws`

    // создаём STOMP Client
    this.client = new Client({
      brokerURL,
      reconnectDelay: 5000,
      debug: msg => console.debug('[STOMP]', msg),
    })

    // автоматически подпишемся на все ранее зарегистрированные топики
    this.client.onConnect = () => {
      console.log('STOMP Connected to', brokerURL)
      this.subscriptions.forEach(({ topic, handler }) => {
        this.client.subscribe(topic, handler)
      })
    }

    this.client.onStompError = frame => {
      console.error('STOMP ERROR:', frame)
    }

    // стартуем соединение
    this.client.activate()
  }

  /**
   * Зарегистрировать подписку на топик.
   * Если клиент уже подключен — подпишемся сразу.
   * @param {string} topic — например `/topic/games/${gameId}`
   * @param {(message: Frame) => void} handler
   */
  subscribe(topic, handler) {
    this.subscriptions.push({ topic, handler })
    if (this.client && this.client.connected) {
      this.client.subscribe(topic, handler)
    }
  }

  /** Отключиться и сбросить все подписки */
  disconnect() {
    if (this.client) {
      this.client.deactivate()
    }
    this.client = null
    this.gameId = null
    this.subscriptions = []
  }
}

// Экспортируем единый экземпляр
export const wsService = new WsService()
