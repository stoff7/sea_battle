<template>
    <div class="game-view">
        <h1>Sea Battle</h1>
        <div class="game-board">
            <!-- Game board will go here -->
        </div>
        <div class="controls">
            <!-- Game controls will go here -->
        </div>
    </div>
</template>

<script>
export default {
    name: "GameView",
    data() {
        return {
            // Add your data properties here
        };
    },
    methods: {
        // Add your methods here
    },
};
</script>

<style scoped>
.game-view {
    text-align: center;
    padding: 20px;
}

.game-board {
    margin: 20px auto;
    width: 300px;
    height: 300px;
    border: 1px solid #000;
}

.controls {
    margin-top: 20px;
}
</style>