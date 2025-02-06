<template>
  <span class="title-text">{{ title }}</span>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
});
</script>

<style lang="scss" scoped>

.title-text {
  font-weight: bold;
}
</style>
