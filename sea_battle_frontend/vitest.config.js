// vitest.config.js
import { defineConfig, configDefaults } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// Мы не спредим сюда всю функцию from vite.config.js, а дублируем только нужные поля
export default defineConfig({
  // Явно прописываем тот же alias, что и в vite.config.js
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  plugins: [
    vue(),
  ],
  test: {
    environment: 'jsdom',
    globals: true,
    exclude: [
      ...configDefaults.exclude,
      'e2e/**',
    ],
  },
})
