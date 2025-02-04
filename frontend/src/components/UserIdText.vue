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

<style lang="scss" scoped>
@import "@/styles/mixins.scss";

.username-text {
  font-weight: bold;
}
</style>
