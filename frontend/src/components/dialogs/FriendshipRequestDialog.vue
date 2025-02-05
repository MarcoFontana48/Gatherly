<script setup lang="ts">
import { ref, onMounted } from 'vue';
import UserIdText from "../text/UsernameText.vue";
import AcceptButton from "@/components/buttons/AcceptButton.vue";
import DeclineButton from "@/components/buttons/DeclineButton.vue";
import Dialog from "@/components/dialogs/Dialog.vue";
import axios from 'axios';

// const senderId = ref("<sender-id>");
const senderId = ref("test3@gmail.com");  //FIXME: tmp for testing purposes (delete this row after test is completed)
const acceptButtonLabel = ref("Accept");
const denyButtonLabel = ref("Reject");
const showRequest = ref(false);

const acceptRequest = async () => {
  try {
    const payload = {
      from: senderId.value,
      to: "test@gmail.com" // TODO: replace with the actual receiver's email (the user using the app)
    };

    const response = await axios.put('http://localhost:8081/friends/requests/accept', payload, {
      headers: { 'Content-Type': 'application/json' }
    });

    console.log('Friendship request declined:', response.data);
  } catch (error) {
    console.error('Error declining friendship request:', error);
  }
};

const denyRequest = async () => {
  try {
    const payload = {
      from: senderId.value,
      to: "test@gmail.com" // TODO: replace with the actual receiver's email (the user using the app)
    };

    const response = await axios.put('http://localhost:8081/friends/requests/decline', payload, {
      headers: { 'Content-Type': 'application/json' }
    });

    console.log('Friendship request accepted:', response.data);
  } catch (error) {
    console.error('Error accepting friendship request:', error);
  }
};

onMounted(() => {
  console.log("mounted");
  showRequest.value = true; //FIXME: tmp, for testing purposes (delete this row after test is completed)
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
  @include default-text-styles($bg-color);
  @include default-dialog-style($bg-color);

  .buttons {
    @include default-align-buttons($gap);
  }
}
</style>
