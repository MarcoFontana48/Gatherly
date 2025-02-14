<script setup lang="ts">
import {ref, computed, watch, onMounted, onBeforeUnmount, inject, onUnmounted} from "vue";
import axios from "axios";
import { useAuthStore } from "@/utils/auth.js";
import BaseInput from "@/components/inputs/BaseInput.vue";
import FriendshipMenuSection from "@/components/friendship/FriendshipMenuSection.vue";
import FriendshipMenuSeparator from "@/components/friendship/FriendshipMenuSeparator.vue";
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import AcceptButton from "@/components/buttons/AcceptButton.vue";
import DeclineButton from "@/components/buttons/DeclineButton.vue";
import Icon from "@/components/images/Icon.vue";
import chatIcon from "@/assets/message-solid.svg";
import ErrorText from "@/components/text/ErrorText.vue";
import {validateEmail} from "@/utils/validator.js";
import {defineSseEventSource} from "@/utils/sse.js";
import FriendshipNotificationSection from "@/components/friendship/FriendshipNotificationSection.vue";
import ChatDialog from "@/components/dialogs/ChatDialog.vue";
import CorrectResponseText from "@/components/text/CorrectResponseText.vue";

const friendships = ref<any[]>([]);
const friendshipRequests = ref<any[]>([]);
const authStore = useAuthStore();
const email = computed(() => authStore.authToken);
const friendEmail = ref("");
const errorMessage = ref("");
const correctResponseText = ref("");
const props = defineProps<{ show: boolean }>();
const emit = defineEmits<{ (event: "close"): void }>();
const showChat = ref(false);
const selectedFriendId = ref("");
const altChatIcon = 'Chat icon';

const events: string[] | undefined = inject("friendshipEvents");
let eventSource: EventSource | null = null;

/**
 * Function to fetch friendships, sends a GET request to the server to retrieve the friendships of the user
 */
const fetchFriendships = async () => {
  if (!email.value) return;

  try {
    console.log("Fetching friendships of userId: " + email.value);
    const response = await axios.get("http://localhost:8081/friends/friendships", { params: { id: email.value } });
    console.log("Friendships fetched:", response.data);
    friendships.value = response.data; //! assuming the response contains an array of friendships
    console.log("Friendships:", friendships.value);
  } catch (error) {
    console.error("Failed to fetch friendships:", error);
  }
};

/**
 * Function to fetch friendship requests, sends a GET request to the server to retrieve the friendship requests of the user
 */
const fetchFriendshipRequests = async () => {
  if (!email.value) return;

  try {
    console.log("Fetching friendships of userId: " + email.value);
    const response = await axios.get("http://localhost:8081/friends/requests", { params: { id: email.value } });
    console.log("Friendship requests fetched:", response.data);
    friendshipRequests.value = response.data.filter((request: any) => {
      return email.value !== request.from.id.value;
    });
    console.log("Friendship Requests:", friendshipRequests.value);
  } catch (error) {
    console.error("Failed to fetch friendship requests:", error);
  }
};

const refreshFriendshipsRelatedData = async () => {
  await fetchFriendships();
  await fetchFriendshipRequests();
};

/**
 * Function to send a friendship request, sends a POST request to the server to send a friendship request to a user
 */
const sendFriendshipRequest = async () => {
  if (!friendEmail.value) return;
  errorMessage.value = "";
  correctResponseText.value = "";

  if (!validateEmail(friendEmail.value)) {
    errorMessage.value =
        "The email you have inserted is not formatted properly.\nA valid email should be in the format:\nemail@something.domain";
    return;
  }

  try {
    const response = await axios.post("http://localhost:8081/friends/requests/send", {
      from: email.value,
      to: friendEmail.value,
    });
    console.log("Friendship request sent:", response.data);
    correctResponseText.value = "Friendship request sent!";
  } catch (error: any) {
    console.error("Failed to send friendship request:", error);
    errorMessage.value = error.response.data;
  }
};

/**
 * Updates the friendships and friendship requests when the component is shown
 */
watch(
    () => [props.show, email],
    ([newShow, newEmail]) => {
      if (newShow && newEmail) {
        fetchFriendships();
        fetchFriendshipRequests();
      }
    },
    { immediate: true }
);

/**
 * Watches for events and updates the friendships and friendship requests when a sse event is received
 */
watch(email, (newEmail) => {
  if (newEmail) {
    eventSource = defineSseEventSource(newEmail, "localhost", "8081");

    if (eventSource) eventSource.onmessage = (event: MessageEvent<any>) => {
      console.log(events)
      console.log('Received event:', event.data);
      const data = JSON.parse(event.data);
      if (events && events.includes(data.topic)) {
        fetchFriendshipRequests();
        fetchFriendships();
      }
    };
  }
}, { immediate: true });

onUnmounted(() => {
  if (eventSource) {
    eventSource.close();
    console.log('EventSource closed');
  }
});

/**
 * Closes the menu
 */
const closeMenu = () => {
  emit("close");
};

/**
 * Accepts a friendship request
 * @param from the user id of the user who sent the request
 */
const acceptRequest = async (from: string) => {
  try {
    const payload = {
      from: from,
      to: email.value,
    };

    const response = await axios.put('http://localhost:8081/friends/requests/accept', payload, {
      headers: { 'Content-Type': 'application/json' }
    });

    console.log('Friendship request accepted:', response.data);

    // refresh friendship requests and friendships
    await fetchFriendshipRequests();
    await fetchFriendships();
  } catch (error) {
    console.error('Error accepting friendship request:', error);
  }
};

/**
 * Declines a friendship request
 * @param from the user id of the user who sent the request
 */
const declineRequest = async (from: string) => {
  try {
    const payload = {
      from: from,
      to: email.value,
    };

    const response = await axios.put('http://localhost:8081/friends/requests/decline', payload, {
      headers: { 'Content-Type': 'application/json' }
    });

    console.log('Friendship request declined:', response.data);
    await fetchFriendshipRequests();
  } catch (error) {
    console.error('Error declining friendship request:', error);
  }
};

/**
 * Opens a chat with a friend
 * @param friendId the id of the friend to open a chat with
 */
const openChatWith = (friendId: string) => {
  if (selectedFriendId.value === friendId) {
    showChat.value = !showChat.value;
  } else {
    selectedFriendId.value = friendId;
    showChat.value = true;
  }
};

/**
 * Closes the menu when the user clicks outside of it
 * @param event the click event
 */
onMounted(() => {
  if (props.show) {
    document.addEventListener("click", closeMenu);
  }
});

/**
 * Removes the event listener when the component is unmounted
 */
onBeforeUnmount(() => {
  document.removeEventListener("click", closeMenu);
});
</script>

<template>
  <div v-if="show" class="overlay" @click.stop="closeMenu">
    <FriendshipNotificationSection class="friendship-notification-section" @refreshFriendships="refreshFriendshipsRelatedData" @click.stop/>
    <div class="desktop-chat-dialog">
      <ChatDialog :show="showChat" :friendId="selectedFriendId" @close="showChat = false" />
    </div>

    <div class="side-panel" @click.stop>
      <div class="panel-header">
        <h3>Friends Menu</h3>
        <button class="close-btn" @click="closeMenu">&times;</button>
      </div>

      <div class="send-friendship-request-section">
        <FriendshipMenuSection title="Send a friendship request" />
        <div class="input">
          <BaseInput v-model="friendEmail" type="email" class="email-input" placeholder="Your friend's email" />
          <NeutralButton @click="sendFriendshipRequest" class="menu-button">Send Request</NeutralButton>
        </div>
        <p>
          <CorrectResponseText v-if="correctResponseText" :text="correctResponseText" />
        </p>
        <p>
          <ErrorText v-if="errorMessage" :text="errorMessage" />
        </p>
      </div>

      <FriendshipMenuSeparator />

      <FriendshipMenuSection title="Pending received friendship requests" />
      <ul class="request-list">
        <li v-for="request in friendshipRequests" :key="request.id" class="friendship-request-section">
          <div class="friend-name-text-container">
            {{ request.from.id.value }}
          </div>
          <div class="handle-friendship-request-button">
            <AcceptButton @click="acceptRequest(request.from.id.value)" class="chat-button">Accept</AcceptButton>
            <DeclineButton @click="declineRequest(request.from.id.value)" class="chat-button">Decline</DeclineButton>
          </div>
        </li>
      </ul>

      <FriendshipMenuSeparator />

      <FriendshipMenuSection title="Your friends" />
      <ul class="friend-list">
        <li v-for="friendship in friendships" :key="friendship.id" class="friendship-section">
          <div class="friend-name-text-container">
            {{ friendship.id.value }}
          </div>
          <div class="chat-button">
            <NeutralButton @click="openChatWith(friendship.id.value)">
              <Icon :src="chatIcon" :alt="altChatIcon" />
            </NeutralButton>
          </div>
        </li>
      </ul>

      <div class="mobile-chat-dialog">
        <ChatDialog :show="showChat" :friendId="selectedFriendId" @close="showChat = false" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.overlay {
  @include mixins.overlay-background;
  @include mixins.default-text-styles(global.$bg-color);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;

  .friendship-notification-section {
    justify-content: flex-end;
    margin: 1vw;
    overflow-y: auto;
    width: 25%;
  }

  .side-panel {
    @include mixins.side-panel(global.$bg-color);
    padding: 2%;
    max-width: 33%;
    width: 100%;
    display: flex;
    flex-direction: column;
    max-height: 100vh;
    height: 100vh;

    @media (max-width: global.$mobile-screen-size) {
      max-width: 96%;
      max-height: 100%;
      height: 100vh;
      width: 100%;
      position: fixed;
      top: 0;
      left: 0;
      background-color: global.$bg-color;
    }

    .panel-header {
      @include mixins.panel-header-styles;

      .close-btn {
        @include mixins.default-close-btn-style;
      }
    }

    ul {
      list-style-type: none;
      padding: 0;

      li {
        @include mixins.align-horizonally-to(center);
        margin: 1vh;
      }
    }

    .send-friendship-request-section {
      .input {
        @include mixins.default-align-items(1%);

        .email-input {
          width: 66%;
        }
        .menu-button {
          width: 33%;
        }
      }
    }

    .friendship-request-section {
      @include mixins.default-align-items(1%);

      .friend-name-text-container {
        @include mixins.break-word-and-ellipsis;
        max-width: 50%;
      }

      .handle-friendship-request-button {
        @include mixins.align-horizonally-to(center);
        gap: 1%;
        max-width: 50%;
        margin-right: 2%
      }
    }

    .friendship-section {
      @include mixins.default-align-items(1%);

      .friend-name-text-container {
        @include mixins.break-word-and-ellipsis;
        max-width: 50%;
      }

      .chat-button {
        max-width: 50%;
        margin-right: 2%
      }
    }

    .mobile-chat-dialog {
      @include mixins.default-chat-style;
      width: 95%;
      bottom: 2%;
      position: fixed;
      left: 0;

      @media (min-width: global.$mobile-screen-size) {
        display: none;
      }
    }
  }

  .desktop-chat-dialog {
    @include mixins.default-chat-style;
    @include mixins.default-background-styles(global.$bg-color);
    position: fixed;
    bottom: 0;
    left: 0;

    @media (max-width: global.$mobile-screen-size) {
      display: none;
    }
  }
}

.friend-list, .request-list {
  max-height: 25vh;
  overflow-y: auto;
}


@keyframes slide-in {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}
</style>