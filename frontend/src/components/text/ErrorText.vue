<template>
  <span class="error-text">{{ text }}</span>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    required: true,
  },
});
</script>

<style scoped lang="scss">
@import "@/styles/mixins";

.error-text {
  color: red;
  white-space: pre-line;
}
</style>
