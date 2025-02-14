<template>
  <BaseButton class="pages-button">
    <div class="button-content">
      <slot></slot>
    </div>
  </BaseButton>
</template>

<script setup>
import BaseButton from './BaseButton.vue';
</script>

<style scoped lang="scss">
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.pages-button {
  $button-bg-color: global.$bg-color;

  @include mixins.adapt-border-color-based-on-bg($button-bg-color, global.$mix-color-amount, $button-bg-color);
  @include mixins.default-text-styles($button-bg-color);
  @include mixins.default-background-styles($button-bg-color);

  &:hover {
    @include mixins.adapt-color-based-on-bg($button-bg-color, global.$hover-darken-amount, $button-bg-color);
  }

  &:active {
    @include mixins.adapt-color-based-on-bg($button-bg-color, global.$active-darken-amount, $button-bg-color);
  }
}

.button-content {
  @include mixins.align-to(center);
}
</style>
