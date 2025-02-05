<template>
  <BaseButton class="neutral-button">
    <slot></slot>
  </BaseButton>
</template>

<script setup>
import BaseButton from './BaseButton.vue';
</script>

<style scoped lang="scss">
@import "@/styles/mixins";
@import "@/styles/global";

.neutral-button {
  $button-bg-color: $bg-color;

  @include adapt-border-color-based-on-bg($button-bg-color, $mix-color-amount, invert($button-bg-color));
  @include default-text-styles($button-bg-color);
  @include default-background-styles($button-bg-color);
  @include align-horizonally-to(center);

  &:hover {
    @include adapt-color-based-on-bg($button-bg-color, $hover-darken-amount, $button-bg-color);
  }

  &:active {
    @include adapt-color-based-on-bg($button-bg-color, $active-darken-amount, $button-bg-color);
  }
}
</style>
