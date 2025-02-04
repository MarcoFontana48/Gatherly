<script setup lang="ts">
import { ref, onMounted } from 'vue';
import UserIdText from "../user/UserIdText.vue";
import AcceptButton from "@/components/buttons/AcceptButton.vue";
import DeclineButton from "@/components/buttons/DeclineButton.vue";
import Dialog from "@/components/dialogs/Dialog.vue";

const senderId = ref("<sender-id>");
const acceptButtonLabel = ref("Accept");
const denyButtonLabel = ref("Reject");
const showRequest = ref(false);

const acceptRequest = () => {
  console.log(`Friendship request from ${senderId.value} accepted`);
};

const denyRequest = () => {
  console.log(`Friendship request from ${senderId.value} denied`);
};

onMounted(() => {
  console.log("mounted");
  showRequest.value = true;
  const eventSource = new EventSource('http://localhost:8081/notifications?id=test');

  eventSource.onmessage = (event) => {
    console.log('Received event:', event.data);

    const data = JSON.parse(event.data);
    senderId.value = data.sender;
    showRequest.value = true;

    setTimeout(() => {
      showRequest.value = false;
    }, 10_000);
  };
});
</script>

<template>
  <Dialog class="friend-request" :showModal="showRequest" @update:showModal="showRequest = $event">
    <template #header>
      <h3>Friend Request</h3>
    </template>

    <template #body>
      <p class="request-text">
        <UserIdText :text="senderId"></UserIdText>
        wants to add you as a friend.
      </p>
    </template>

    <template #footer>
      <div class="buttons">
        <AcceptButton @click="acceptRequest">{{ acceptButtonLabel }}</AcceptButton>
        <DeclineButton @click="denyRequest">{{ denyButtonLabel }}</DeclineButton>
      </div>
    </template>
  </Dialog>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

$gap: 1vw;

.friend-request {
  @include default-text-styles(invert($bg-color));
  @include default-dialog-style($bg-color);

  .buttons {
    margin-top: $gap;
    display: flex;
    justify-content: space-between;
    gap: $gap;
    width: 100%;
  }
}
</style>
