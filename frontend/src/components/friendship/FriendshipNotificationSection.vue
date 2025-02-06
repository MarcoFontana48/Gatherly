<script setup lang="ts">
import {ref, watch, computed, inject} from 'vue';
import UserIdText from "../text/UsernameText.vue";
import AcceptButton from "@/components/buttons/AcceptButton.vue";
import DeclineButton from "@/components/buttons/DeclineButton.vue";
import Dialog from "@/components/dialogs/Dialog.vue";
import axios from 'axios';
import {useAuthStore} from "@/utils/auth.js";
import {defineSseEventSource} from "@/utils/sse.js";
import NeutralButton from "@/components/buttons/NeutralButton.vue";

// const senderId = ref("<sender-id>");
const authStore = useAuthStore();
const email = computed(() => authStore.authToken);
const friendRequests = ref<{ senderId: string; id: number }[]>([]);
const friendshipNotifications = ref<{ message: string; id: number }[]>([]);

const acceptRequest = async (index: number) => {
  try {
    const request = friendRequests.value[index];

    await axios.put('http://localhost:8081/friends/requests/accept', {
      from: request.senderId,
      to: email.value,
    });

    // Rimuovi la richiesta dalla lista
    friendRequests.value.splice(index, 1);
  } catch (error) {
    console.error('Error accepting friendship request:', error);
  }
};

const denyRequest = async (index: number) => {
  try {
    const request = friendRequests.value[index];

    await axios.put('http://localhost:8081/friends/requests/decline', {
      from: request.senderId,
      to: email.value,
    });

    // Rimuovi la richiesta dalla lista
    friendRequests.value.splice(index, 1);
  } catch (error) {
    console.error('Error declining friendship request:', error);
  }
};

const closeNotification = (index: number) => {
  friendshipNotifications.value.splice(index, 1);
};

watch(email, (newEmail) => {
  if (newEmail) {
    const eventSource = defineSseEventSource(newEmail);

    eventSource.onmessage = (event: MessageEvent<any>) => {
      console.log('Received event:', event.data);
      const data = JSON.parse(event.data);

      if (data.topic === "friendship-request-sent") {
        // Aggiungi una nuova richiesta con un ID unico
        friendRequests.value.push({
          senderId: data.sender,
          id: Date.now(),
        });

        // Auto-rimuovere dopo 10 secondi
        setTimeout(() => {
          friendRequests.value.shift();
        }, 10_000);
      }

      // if (data.topic === "friendship-request-accepted" && data.sender === email.value) {
        // Notifica che la richiesta è stata accettata
        friendshipNotifications.value.push({
          message: `${data.receiver} has accepted your friend request.`,
          id: Date.now(),
        });

        // Auto-rimuovere la notifica dopo 5 secondi
        setTimeout(() => {
          friendshipNotifications.value.shift();
        }, 5000);
      // }

      // if (data.topic === "friendship-request-rejected" && data.sender === email.value) {
        // Notifica che la richiesta è stata rifiutata
        friendshipNotifications.value.push({
          message: `${data.receiver} has rejected your friendship request.`,
          id: Date.now(),
        });

        // Auto-rimuovere la notifica dopo 5 secondi
        setTimeout(() => {
          friendshipNotifications.value.shift();
        }, 5000);
      // }
    };
  }
}, { immediate: true });
</script>

<template>
  <div class="friendship-requests">
    <Dialog v-for="(request, index) in friendRequests" :key="request.id" class="friend-request" :showModal="true">
      <template #header>
        <h3>Friend Request</h3>
      </template>

      <template #body>
        <p class="request-text">
          <UserIdText :text="request.senderId"></UserIdText>
          wants to add you as a friend.
        </p>
      </template>

      <template #footer>
        <div class="buttons">
          <AcceptButton @click="acceptRequest(index)">Accept</AcceptButton>
          <DeclineButton @click="denyRequest(index)">Reject</DeclineButton>
        </div>
      </template>
    </Dialog>

    <!-- Dialog per notificare all'utente se la sua richiesta è stata accettata o rifiutata -->
    <Dialog v-for="(notification, index) in friendshipNotifications" :key="notification.id" class="friend-request" :showModal="true">
      <template #header>
        <h3>Friendship Update</h3>
      </template>

      <template #body>
        <p class="notification-text">{{ notification.message }}</p>
      </template>

      <template #footer>
        <div class="buttons">
          <NeutralButton @click="closeNotification(index)">Close</NeutralButton>
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins";
@import "@/styles/global";

$gap: 1vw;

.friendship-requests {
  display: flex;
  flex-direction: column;

  .friend-request {
    @include default-text-styles($bg-color);
    @include default-dialog-style($bg-color);
    width: 300px;

    .buttons {
      @include default-align-buttons($gap);
    }
  }
}

</style>
