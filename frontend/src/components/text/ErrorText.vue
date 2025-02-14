<template>
  <span class="error-text">{{ text }}</span>
</template>

<script setup lang="ts">
defineProps({
  text: {
    type: String,
    required: true,
  },
});
</script>

<style scoped lang="scss">
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.error-text {
  @include mixins.error-text-style(global.$bg-color);
}
</style>
