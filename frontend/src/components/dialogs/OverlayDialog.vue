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
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.modal-overlay {
  @include overlay-background;
  @include align-to(center);
  @include default-text-styles($bg-color);

  .modal-content {
    @include default-text-styles($bg-color);
    @include default-dialog-style($bg-color);
  }
}
</style>
