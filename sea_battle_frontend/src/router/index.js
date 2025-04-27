import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue' // импортируем компонент HomeView.vue
import GameView from '@/views/GameView.vue' // импортируем компонент GameView.vue
import ResultsView from '@/views/ResultsView.vue' // импортируем компонент ResultsView.vue

const routes = [
  { path: '/',      name: 'home',    component: HomeView },
  { path: '/game',  name: 'game',    component: GameView },
  { path: '/results', name: 'results', component: ResultsView },
]

export const router = createRouter({
  history: createWebHistory(),  
  routes,
})
