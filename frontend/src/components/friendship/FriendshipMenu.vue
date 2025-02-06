<script setup lang="ts">
import {ref, computed, watch, onMounted, onBeforeUnmount, provide, inject} from "vue";
import axios from "axios";
import { useAuthStore } from "@/utils/auth.js";
import BaseInput from "@/components/inputs/BaseInput.vue";
import FriendshipMenuSection from "@/components/friendship/FriendshipMenuSection.vue";
import FriendshipMenuSeparator from "@/components/friendship/FriendshipMenuSeparator.vue";
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import AcceptButton from "@/components/buttons/AcceptButton.vue";
import DeclineButton from "@/components/buttons/DeclineButton.vue";
import Icon from "@/components/images/Icon.vue";
import chatIcon from "@/assets/pen-solid.svg";
import ErrorText from "@/components/text/ErrorText.vue";
import {validateEmail} from "@/utils/validator.js";
import {defineSseEventSource} from "@/utils/sse.js";
import FriendshipNotificationSection from "@/components/friendship/FriendshipNotificationSection.vue";

const friendships = ref<any[]>([]);
const friendshipRequests = ref<any[]>([]);
const authStore = useAuthStore();
const email = computed(() => authStore.authToken);
const friendEmail = ref("");
const errorMessage = ref("");
const props = defineProps<{ show: boolean }>();
const emit = defineEmits<{ (event: "close"): void }>();

const events: string[] | undefined = inject("friendshipEvents");

// Function to retrieve friendships
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

// Function to retrieve friendship requests
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

// Function to send a friendship request
const sendFriendshipRequest = async () => {
  if (!friendEmail.value) return;
  errorMessage.value = "";

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
  } catch (error: any) {
    console.error("Failed to send friendship request:", error);
    errorMessage.value = error.response.data;
  }
};

// Watch for changes in 'show' prop and user authentication
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

watch(email, (newEmail) => {
  if (newEmail) {
    const eventSource = defineSseEventSource(newEmail)

    eventSource.onmessage = (event: MessageEvent<any>) => {
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

// Close the menu when clicking outside
const closeMenu = () => {
  emit("close");
};

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

onMounted(() => {
  if (props.show) {
    document.addEventListener("click", closeMenu);
  }
});

onBeforeUnmount(() => {
  document.removeEventListener("click", closeMenu);
});
</script>

<template>
  <div v-if="show" class="overlay" @click.stop="closeMenu">
    <FriendshipNotificationSection class="friendship-notification-section"/>

    <div class="side-panel" @click.stop>
      <div class="panel-header">
        <h3>Friends Menu</h3>
        <button class="close-btn" @click="closeMenu">&times;</button>
      </div>

      <FriendshipMenuSection title="Send a friendship request" />
      <BaseInput v-model="friendEmail" type="email" placeholder="Your friend's email" />
      <NeutralButton @click="sendFriendshipRequest" class="menu-button">Send Request</NeutralButton>
      <p>
        <ErrorText v-if="errorMessage" :text="errorMessage" class="error-message" />
      </p>

      <FriendshipMenuSeparator />

      <!-- Upper part: Friendship Requests -->
      <FriendshipMenuSection title="Pending received friendship requests" />
      <ul>
        <li v-for="request in friendshipRequests" :key="request.id">
          {{ request.from.id.value }}
            <AcceptButton @click="acceptRequest(request.from.id.value)" class="chat-button">Accept</AcceptButton>
            <DeclineButton @click="declineRequest(request.from.id.value)">Decline</DeclineButton>
        </li>
      </ul>

      <FriendshipMenuSeparator />

      <!-- Lower part: Friendships -->
      <FriendshipMenuSection title="Your friends" />
      <ul>
        <li v-for="friendship in friendships" :key="friendship.id">
          {{ friendship.id.value }}
          <NeutralButton @click="openChatWith(friendship.id.value)" class="chat-button">
            <Icon :src="chatIcon" :alt="chatIcon" />
            Open chat
          </NeutralButton>
        </li>
      </ul>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins";
@import "@/styles/global";

.overlay {
  @include overlay-background;
  @include default-text-styles($bg-color);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
}

.side-panel {
  @include side-panel($bg-color);
  padding: 5vh;
  max-width: 66vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  @include panel-header-styles;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  @include align-horizonally-to(center);
  margin: 1vh;
}

.close-btn {
  @include default-close-btn-style;
}

.chat-button {
  margin-left: 20%;
}

.friendship-notification-section {
  justify-content: flex-end;
  margin: 1vw;
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
