import { createApp } from 'vue'
import App from './App.vue'
import { router } from './router'           // импортируем маршрутизатор
import { createPinia } from 'pinia'         

const app = createApp(App)
app.use(router)                             //  подключаем Vue Router
app.use(createPinia())                      // ←подключаем Pinia 
app.mount('#app')
