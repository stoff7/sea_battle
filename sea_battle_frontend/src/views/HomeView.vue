<template>
    <div>
      <h1>Главная</h1>
      <router-link to="/game">Начать игру</router-link>
    </div>
  </template>
  

<script>
export default {
    name: "HomeView",
};
</script>

<style scoped>
.home {
    text-align: center;
    margin-top: 20px;
}
</style>