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
@import "@/styles/global.scss";
@import "@/styles/mixins.scss";

.modal-content {
  @include default-dialog-style($bg-color);
}
</style>
