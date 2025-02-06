<script setup lang="ts">
import Dialog from "@/components/dialogs/Dialog.vue";
import NeutralButton from "@/components/buttons/NeutralButton.vue";

defineProps<{
  notifications: { message: string; id: number }[];
}>();

const emit = defineEmits(["close"]);
</script>

<template>
  <Dialog v-for="(notification, index) in notifications" :key="notification.id" class="friend-notification" :showModal="true">
    <template #header>
      <h3>Friendship Update</h3>
    </template>

    <template #body>
      <p class="notification-text">{{ notification.message }}</p>
    </template>

    <template #footer>
      <div class="buttons">
        <NeutralButton @click="emit('close', index)">Close</NeutralButton>
      </div>
    </template>
  </Dialog>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.friend-notification {
  @include default-dialog-style($bg-color, red);
}

</style>