<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';

const props = defineProps<{
  showModal: boolean;
}>();

const emit = defineEmits(['update:showModal']);
</script>

<template>
  <div v-if="props.showModal" class="modal-overlay" @click="emit('update:showModal', false)">
    <div class="modal-content" @click.stop>
      <slot name="header"></slot>
      <slot name="body"></slot>
      <slot name="footer">
        <button @click="emit('update:showModal', false)">Close</button>
      </slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  width: 300px;
  text-align: center;
}
</style>
