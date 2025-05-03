<template>
    <div
      class="ship-placeholder" :draggable="true" @dragstart="onDragStart" :style= "{ width: width + 'px', height: height + 'px' }">
      {{ name }}
    </div>
  </template>
  
  <script setup>
  import { defineProps } from 'vue'
  const props = defineProps({
    id: Number,
    name: String,
    width: Number,
    height: Number
  })
  function onDragStart(e) {
  const payload = {
    id: props.id,
    type: props.name,
    length: props.length
  }
  e.dataTransfer.setData('application/json', JSON.stringify(payload))  // :contentReference[oaicite:3]{index=3}
}
  </script>
  
  <style scoped>
  .ship-placeholder {
    background: #ccc;
    border: 1px solid #999;
    text-align: center;
    line-height: normal;
    cursor: grab;
  }
  </style>
  