<script setup lang="ts">
import {ref, watch, computed, onUnmounted, defineEmits} from 'vue';
import FriendshipRequestDialog from "@/components/dialogs/FriendshipRequestDialog.vue";
import FriendshipNotificationDialog from "@/components/dialogs/FriendshipNotificationDialog.vue";
import axios from 'axios';
import { useAuthStore } from "@/utils/auth.js";
import { defineSseEventSource } from "@/utils/sse.ts";

const authStore = useAuthStore();
const email = computed(() => authStore.authToken);

const friendRequests = ref<{ senderId: string; id: number }[]>([]);
const friendshipNotifications = ref<{ message: string; id: number }[]>([]);

let eventSource: EventSource | null = null;

const emit = defineEmits(['refreshFriendships']);

/**
 * Accept a friendship request
 * @param index - Index of the request to accept
 */
const acceptRequest = async (index: number) => {
  try {
    const request = friendRequests.value[index];

    await axios.put('http://localhost:8081/friends/requests/accept', {
      from: request.senderId,
      to: email.value,
    });

    friendRequests.value.splice(index, 1);

    // emit event to the parent component to call fetchFriendships in order to update the UI
    emit('refreshFriendships');
  } catch (error) {
    console.error('Error accepting friendship request:', error);
  }
};

/**
 * Deny a friendship request
 * @param index - Index of the request to deny
 */
const denyRequest = async (index: number) => {
  try {
    const request = friendRequests.value[index];

    await axios.put('http://localhost:8081/friends/requests/decline', {
      from: request.senderId,
      to: email.value,
    });

    friendRequests.value.splice(index, 1);
  } catch (error) {
    console.error('Error declining friendship request:', error);
  }
};

/**
 * Close a notification
 * @param index - Index of the notification to close
 */
const closeNotification = (index: number) => {
  friendshipNotifications.value.splice(index, 1);
};

/**
 * Listens for friendship events via SSE and updates the UI accordingly
 */
watch(email, (newEmail) => {
  if (!newEmail) return;

  eventSource = defineSseEventSource(newEmail, "localhost", "8081");

  if (eventSource) eventSource.onmessage = (event: MessageEvent<any>) => {
    console.log('Received event:', event.data);
    const data = JSON.parse(event.data);

    const addWithTimeout = (list: any[], item: any, timeout: number) => {
      list.push(item);
      setTimeout(() => list.shift(), timeout);
    };

    switch (data.topic) {
      case "friendship-request-sent":
        addWithTimeout(friendRequests.value, { senderId: data.sender, id: Date.now() }, 10_000);
        break;

      case "friendship-request-accepted":
      case "friendship-request-rejected":
        const message = data.topic === "friendship-request-accepted"
            ? `${data.sender} has accepted your friend request.`
            : `${data.sender} has rejected your friend request.`;

        addWithTimeout(friendshipNotifications.value, { message, id: Date.now() }, 5000);
        break;
    }
  };
}, { immediate: true });

onUnmounted(() => {
  if (eventSource) {
    eventSource.close();
    console.log('EventSource closed');
  }
});
</script>

<template>
  <div class="friendship-requests">
    <FriendshipRequestDialog :requests="friendRequests" @accept="acceptRequest" @reject="denyRequest" @close="closeNotification"/>
    <FriendshipNotificationDialog :notifications="friendshipNotifications" @close="closeNotification"/>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";

.friendship-requests {
  @include align-vertically-to(flex-start);
  margin-right: 5%;
}

</style>
