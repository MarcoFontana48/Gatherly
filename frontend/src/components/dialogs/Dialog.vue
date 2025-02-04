<script setup lang="ts">
import { defineProps, defineEmits, defineSlots } from 'vue';

const props = defineProps<{
  showModal: boolean;
}>();

const emit = defineEmits(['update:showModal']);

const closeModal = () => {
  emit('update:showModal', false);
};
</script>

<template>
  <div v-if="props.showModal" class="modal-content">
    <slot name="header"></slot>
    <slot name="body"></slot>
    <slot name="footer">
      <button @click="closeModal">Close</button>
    </slot>
  </div>
</template>

<style lang="scss" scoped>
.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  width: 300px;
  text-align: center;
}
</style>
