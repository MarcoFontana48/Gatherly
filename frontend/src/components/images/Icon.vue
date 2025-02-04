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
@import "@/styles/mixins.scss";

.icon {
  @include default-icon-size;
  @include default-icon-margin;
}
</style>
