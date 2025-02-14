<template>
  <button class="base-button">
    <slot></slot>
  </button>
</template>

<script setup>

</script>

<style scoped lang="scss">
@use "@/styles/mixins" as mixins;

.base-button {
  @include mixins.default-button-style;
}
</style>
