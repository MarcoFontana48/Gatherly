<template>
  <BaseButton class="neutral-button">
    <slot></slot>
  </BaseButton>
</template>

<script setup>
import BaseButton from './BaseButton.vue';
</script>

<style scoped lang="scss">
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;
@use "sass:color";

.neutral-button {
  $button-bg-color: global.$bg-color;

  @include mixins.adapt-border-color-based-on-bg($button-bg-color, global.$mix-color-amount, color.invert(global.$bg-color));
  @include mixins.default-text-styles($button-bg-color);
  @include mixins.default-background-styles($button-bg-color);
  @include mixins.align-horizonally-to(center);

  &:hover {
    @include mixins.adapt-color-based-on-bg($button-bg-color, global.$hover-darken-amount, $button-bg-color);
    @include mixins.adapt-background-color-based-on-bg($button-bg-color, 10%, $button-bg-color);
  }

  &:active {
    @include mixins.adapt-color-based-on-bg($button-bg-color, global.$active-darken-amount, $button-bg-color);
    @include mixins.adapt-background-color-based-on-bg($button-bg-color, 20%, $button-bg-color);
  }
}
</style>
