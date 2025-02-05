<script setup lang="ts">
import { defineProps, defineEmits, ref, watch } from 'vue';

const props = defineProps<{ modelValue: string; placeholder?: string; type?: string }>();
const emit = defineEmits(['update:modelValue']);

const inputValue = ref(props.modelValue);

// Watch for changes and emit updates
watch(inputValue, (newValue) => {
  emit('update:modelValue', newValue);
});
</script>

<template>
  <div class="input-container">
    <input
        v-model="inputValue"
        :type="type || 'text'"
        :placeholder="placeholder"
        class="custom-input"
    />
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.custom-input {
  @include default-text-styles($bg-color);
  @include default-input-form-styles($bg-color);
  margin-bottom: 1vh;
}
</style>
