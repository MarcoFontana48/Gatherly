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
@import "@/styles/mixins";
@import "@/styles/global";

.pages-button {
  $button-bg-color: $bg-color;

  @include adapt-border-color-based-on-bg($button-bg-color, $mix-color-amount, $button-bg-color);
  @include default-text-styles($button-bg-color);
  @include default-background-styles($button-bg-color);

  &:hover {
    @include adapt-color-based-on-bg($button-bg-color, $hover-darken-amount, $button-bg-color);
  }

  &:active {
    @include adapt-color-based-on-bg($button-bg-color, $active-darken-amount, $button-bg-color);
  }
}

.button-content {
  @include align-to(center);
}
</style>
