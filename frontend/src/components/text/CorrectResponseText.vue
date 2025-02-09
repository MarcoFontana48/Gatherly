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
@import "@/styles/mixins";
@import "@/styles/global";

.correct-response-text {
  @include correct-response-text-style($bg-color);
}
</style>