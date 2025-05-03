import { createApp } from 'vue'
import App from './App.vue'
import { router } from './router'           // импортируем маршрутизатор
import { createPinia } from 'pinia'



const app = createApp(App)
app.use(router)                             //  подключаем Vue Router
app.use(createPinia())                      // ←подключаем Pinia 
app.mount('#app')

app.directive('debounce', {
    mounted(el, binding) {
      let timeout

      const delay = parseInt(binding.value) || 700
      const handler = () => {
        clearTimeout(timeout)
        timeout = setTimeout(() => {

          el.dispatchEvent(new Event('debounced-input'))
        }, delay)
      }
      el.addEventListener('input', handler)

      el._cleanup = () => el.removeEventListener('input', handler)
    },
    unmounted(el) {
      el._cleanup()
    }
  })
  