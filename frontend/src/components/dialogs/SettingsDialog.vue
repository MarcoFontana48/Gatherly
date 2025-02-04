<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import OverlayDialog from './OverlayDialog.vue';
import DeclineButton from '../buttons/DeclineButton.vue';

const props = defineProps<{
  showModal: boolean;
}>();

const emit = defineEmits(['update:showModal']);

// Function to close the modal
const closeModal = () => {
  emit('update:showModal', false);
};
</script>

<template>
  <OverlayDialog :showModal="props.showModal" @update:showModal="emit('update:showModal', $event)">
    <template #header>
      <h3>Settings</h3>
    </template>

    <template #body>
      <p>There are currently no settings for this web application</p>
    </template>

    <template #footer>
      <!-- Use DeclineButton to close the modal -->
      <DeclineButton @click="closeModal">Close</DeclineButton>
    </template>
  </OverlayDialog>
</template>

<style lang="scss" scoped>
/* Add your styling here */
</style>
