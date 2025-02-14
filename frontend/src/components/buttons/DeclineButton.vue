<template>
  <BaseButton class="decline-button">
    <slot></slot>
  </BaseButton>
</template>

<script setup>
import BaseButton from './BaseButton.vue';
</script>

<style scoped lang="scss">
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.decline-button {
  $button-bg-color: red;

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
</style>
