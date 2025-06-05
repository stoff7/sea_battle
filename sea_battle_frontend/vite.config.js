import { fileURLToPath, URL } from 'url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig(({ mode }) => {
  // Загружаем переменные окружения
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    define: {
      global: 'window'
    },
    optimizeDeps: {
      include: ['sockjs-client']
    },
    server: {  
      port: 3000,
      host: '0.0.0.0',
      cors: {
        origin: [
          'http://localhost:3000',
          'https://*.ngrok-free.app',
          'https://*.ngrok.io'
        ],
        credentials: true
      },
      allowedHosts: [
        'localhost',
        env.VITE_API_URL && env.VITE_API_URL.replace(/^https?:\/\//, '')
      ].filter(Boolean),
      proxy: {
        '/api': {
          target: 'http://localhost:8000',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    }
  }
})