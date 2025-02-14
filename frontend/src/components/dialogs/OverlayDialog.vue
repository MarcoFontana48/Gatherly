<script setup lang="ts">

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
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.modal-overlay {
  @include mixins.overlay-background;
  @include mixins.align-to(center);
  @include mixins.default-text-styles(global.$bg-color);

  .modal-content {
    @include mixins.default-text-styles(global.$bg-color);
    @include mixins.default-dialog-style(global.$bg-color);
  }
}
</style>
