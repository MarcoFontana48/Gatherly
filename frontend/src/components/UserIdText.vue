<template>
  <span>
    (
    <span class="user-id-text">
      {{ text }}
    </span>
    )
  </span>
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
.user-id-text {
  font-style: italic;
}
</style>
