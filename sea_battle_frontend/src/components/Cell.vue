<template>
    <div class="cell"></div>
  </template>
  
  <script setup>
  import { defineProps } from 'vue'
  defineProps({ row: Number, col: Number })
  </script>
  
  <style scoped>
  .cell {
    width: 40px; height: 40px;
    border: 1px solid #333;
    box-sizing: border-box;
  }
  </style>
  