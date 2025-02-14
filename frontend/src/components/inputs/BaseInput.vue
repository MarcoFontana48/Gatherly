<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{ modelValue: string; placeholder?: string; type?: string }>();
const emit = defineEmits(['update:modelValue']);

const inputValue = ref(props.modelValue);

watch(inputValue, (newValue) => {
  emit('update:modelValue', newValue);
});
</script>

<template>
  <div class="input-container">
    <input v-model="inputValue" :type="type || 'text'" :placeholder="placeholder" class="custom-input" />
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.input-container {
  @include mixins.align-horizonally-to(center);
  width: 100%;

  .custom-input {
    @include mixins.default-text-styles(global.$bg-color);
    @include mixins.default-input-form-styles(global.$bg-color);
    margin-bottom: 1vh;
    margin-top: 1vh;
  }
}
</style>
