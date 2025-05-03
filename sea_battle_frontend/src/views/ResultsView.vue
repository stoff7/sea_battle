<template>
    <div class="results-view">
        <h1>Поздравляем!</h1>
        <p>Вы успешно завершили игру!</p>
        <router-link to=/>
            <button>Вернуться на главную</button>
        </router-link>
    </div>
</template>

<script>
export default {
    name: 'ResultsView',
};
</script>

<style scoped>
.results-view {
    text-align: center;
    margin-top: 50px;
}

button {
    margin-top: 20px;
    padding: 10px 20px;
    font-size: 16px;
    cursor: pointer;
}
</style>
