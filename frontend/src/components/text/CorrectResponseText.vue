<template>
  <span class="correct-response-text">{{ text }}</span>
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

.correct-response-text {
  @include mixins.correct-response-text-style(global.$bg-color);
}
</style>