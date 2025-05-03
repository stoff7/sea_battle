import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import RoomView from '@/views/RoomView.vue'
import JoinRoomView from '@/views/JoinRoomView.vue'
import InBattleView from '@/views/InBattleView.vue'
import ResultsView from '@/views/ResultsView.vue' 

const routes = [
  { path: '/',       name: 'home',      component: HomeView },
  { path: '/results',name: 'results',   component: ResultsView },
  { path: '/room/:roomId',name: 'room',    component: RoomView,props: true },
  { path: '/join',   name: 'join',    component: JoinRoomView},
  { path: '/inbattle/:roomId',name: 'inbattle',component: InBattleView,props: true },
]

export const router = createRouter({
  history: createWebHistory(),  
  routes,
})