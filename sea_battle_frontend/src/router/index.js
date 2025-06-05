import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import RoomView from '@/views/RoomView.vue'
import InBattleView from '@/views/InBattleView.vue'

const routes = [
  { path: '/',       name: 'home',      component: HomeView },
  { path: '/room/:gameId',name: 'room',    component: RoomView,props: true },
  { path: '/inbattle/:gameId',name: 'inbattle',component: InBattleView,props: route => ({
    gameId: route.params.gameId
  }) },
]

export const router = createRouter({
  history: createWebHistory(),  
  routes,
})