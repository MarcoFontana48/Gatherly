<script setup lang="ts">
import {ref, watch, defineProps, defineEmits, onMounted, onBeforeUnmount, computed} from "vue";
import BaseInput from "@/components/inputs/BaseInput.vue";
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import {useAuthStore} from "@/utils/auth.ts";

const props = defineProps<{ show: boolean; friendId: string }>();
const emit = defineEmits<{ (event: "close"): void }>();
const messages = ref<{ sender: string; content: string }[]>([]);
const newMessage = ref("");
const authStore = useAuthStore();
const email = computed(() => authStore.authToken);
const socket = ref<WebSocket | null>(null);

function clearChat() {
  messages.value = []; // Clear existing chat messages
  newMessage.value = ""; // Clear input field
}

const sendMessage = () => {
  if (!newMessage.value.trim()) return;

  const messageData = {
    sender: email.value,
    receiver: props.friendId,
    content: newMessage.value,
  };

  if (socket.value && socket.value.readyState === WebSocket.OPEN) {
    socket.value.send(JSON.stringify(messageData));
    messages.value.push(messageData);
    newMessage.value = "";
  } else {
    console.error("WebSocket is not open.");
  }
};

watch(() => [props.show, props.friendId], ([newShow, _]) => {
  if (newShow) {
    clearChat();
  }
}, { immediate: true });

const closeDialog = () => {
  emit("close");
};

onMounted(() => {
  const userId = email.value;
  socket.value = new WebSocket(`ws://localhost:8081?id=${userId}`);
  console.log("WebSocket created");

  // Handle incoming messages
  socket.value.onmessage = (event) => {
    console.log("Received message:", event.data);
    const receivedMessage = JSON.parse(event.data);
    messages.value.push(receivedMessage);
  };

  socket.value.onclose = () => {
    console.log("WebSocket closed");
  };
});

onBeforeUnmount(() => {
  socket.value?.close();
});
</script>

<template>
  <transition name="slide-up">
    <div v-if="show" class="chat-dialog" @click.stop>
      <div class="chat-header">
        <h3>Chat with {{ friendId }}</h3>
        <button class="close-btn" @click="closeDialog">&times;</button>
      </div>
      <div class="chat-messages">
        <div v-for="(message, index) in messages" :key="index" class="message">
          <strong>{{ message.sender }}:</strong> {{ message.content }}
        </div>
      </div>
      <div class="chat-input">
        <BaseInput v-model="newMessage" placeholder="Type a message..." class="message-container"/>
        <NeutralButton @click="sendMessage" class="send-button">Send</NeutralButton>
      </div>
    </div>
  </transition>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";

.chat-dialog {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 33vw;
  background: white;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
  box-shadow: 0 -4px 6px rgba(0, 0, 0, 0.1);
  padding: 15px;
  display: flex;
  flex-direction: column;
  max-height: 50vh;
  overflow-y: auto;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.chat-messages {
  flex-grow: 1;
  overflow-y: auto;
  padding: 5px;
  border-bottom: 1px solid #ddd;
}

.message {
  margin-bottom: 5px;
}

.chat-input {
  @include default-align-items(1vw);

  .send-button {
    width: 20%;
  }

  .message-container {
    width: 80%;
  }
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: transform 0.3s ease-in-out;
}

.slide-up-enter-from, .slide-up-leave-to {
  transform: translateY(100%);
}
</style>
