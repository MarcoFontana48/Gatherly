<script setup lang="ts">
import Dialog from "@/components/dialogs/Dialog.vue";
import UserIdText from "@/components/text/UsernameText.vue";
import AcceptButton from "@/components/buttons/AcceptButton.vue";
import DeclineButton from "@/components/buttons/DeclineButton.vue";

defineProps<{
  requests: { senderId: string; id: number }[];
}>();

const emit = defineEmits(["accept", "reject", "close"]);

/**
 * Close the dialog
 * @param index - Index of the dialog to close
 */
const closeDialog = (index: number) => {
  emit("close", index);
};
</script>

<template>
  <Dialog v-for="(request, index) in requests" :key="request.id" class="friend-request" :showModal="true">
    <template #header>
      <h3>Friend Request</h3>
      <button @click="closeDialog(index)" class="close-btn">&times;</button>
    </template>

    <template #body>
      <p class="request-text">
        <UserIdText :text="request.senderId" /> wants to add you as a friend.
      </p>
    </template>

    <template #footer>
      <div class="buttons">
        <AcceptButton @click="emit('accept', index)">Accept</AcceptButton>
        <DeclineButton @click="emit('reject', index)">Reject</DeclineButton>
      </div>
    </template>
  </Dialog>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

$gap: 1vw;

.friend-request {
  @include default-text-styles($bg-color);
  @include default-dialog-style($bg-color, red);

  position: relative;

  .close-btn {
    @include cross-close-button;
    position: absolute;
    top: 2%;
    right: 2%;
  }

  .buttons {
    @include default-align-items($gap);
  }
}
</style>