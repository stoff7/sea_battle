import { createApp } from 'vue'
import App from './App.vue'
import { router } from './router'          
import { createPinia } from 'pinia'
import i18n from './locales/i18n'



const app = createApp(App)
app.use(router)                            
app.use(createPinia())
app.use(i18n)                      
app.mount('#app')

router.afterEach((to) => {
  const { t } = useI18n()
  document.title = t('home.title')
})

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
  