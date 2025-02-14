<script setup lang="ts">
defineProps<{
  src: string;
  alt: string;
}>();
</script>

<template>
  <img :src="src" :alt="alt" class="icon">
</template>

<style scoped lang="scss">
@use "@/styles/mixins" as mixins;

.icon {
  @include mixins.default-icon-size;
  @include mixins.default-icon-margin;
}
</style>
