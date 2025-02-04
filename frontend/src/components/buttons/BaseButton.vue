<template>
  <button class="base-button">
    <slot></slot>
  </button>
</template>

<script setup>

</script>

<style scoped lang="scss">
@import "@/styles/mixins";

.base-button {
  @include default-button-style;
}
</style>
