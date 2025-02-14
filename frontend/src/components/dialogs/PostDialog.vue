<script setup lang="ts">
import Dialog from '@/components/dialogs/Dialog.vue';
import AcceptButton from '@/components/buttons/AcceptButton.vue';
import DeclineButton from '@/components/buttons/DeclineButton.vue';
import BaseInput from "@/components/inputs/BaseInput.vue";

defineProps<{
  show: boolean;
  title: string;
  bodyText?: string;
  input?: boolean;
  modelValue?: string;
}>();

const emit = defineEmits(["update:show", "confirm", "update:modelValue"]);

/**
 * Close the dialog
 */
const closeDialog = () => {
  emit("update:show", false);
};
</script>

<template>
  <Dialog v-if="show" @close="closeDialog" class="fixed-dialog" show-modal>
    <template #header>
      <h3>{{ title }}</h3>
    </template>
    <template #body>
      <p v-if="bodyText">{{ bodyText }}</p>
      <BaseInput v-if="input" :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" placeholder="Write post content here..." />
    </template>
    <template #footer>
      <span class="buttons">
        <AcceptButton @click="$emit('confirm')">Confirm</AcceptButton>
        <DeclineButton @click="closeDialog">Cancel</DeclineButton>
      </span>
    </template>
  </Dialog>
</template>

<style scoped lang="scss">
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.fixed-dialog {
  @include mixins.post-dialog-style(global.$bg-color);

  .buttons {
    @include mixins.default-align-items(1vw);
  }
}
</style>