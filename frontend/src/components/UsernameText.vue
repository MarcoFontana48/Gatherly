<template>
  <span class="username-text">{{ text }}</span>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    required: true,
  },
});
</script>

<style scoped>
.username-text {
  font-weight: bold;
}
</style>
