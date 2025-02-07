<template>
  <p>{{ content }}</p>
</template>

<script setup lang="ts">
defineProps({
  content: {
    type: String,
    required: true,
  },
});
</script>

<style lang="scss" scoped>

</style>