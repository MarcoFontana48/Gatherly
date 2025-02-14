<script setup lang="ts">
import {ref, watch, onMounted, onBeforeUnmount, computed} from "vue";
import BaseInput from "@/components/inputs/BaseInput.vue";
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import {useAuthStore} from "@/utils/auth.ts";
import UsernameText from "@/components/text/UsernameText.vue";
import Icon from "@/components/images/Icon.vue";
import sendIcon from "@/assets/paper-plane-solid.svg";

const props = defineProps<{ show: boolean; friendId: string }>();
const emit = defineEmits<{ (event: "close"): void }>();
const messages = ref<{ sender: string; content: string }[]>([]);
const newMessage = ref("");
const authStore = useAuthStore();
const email = computed(() => authStore.authToken);
const socket = ref<WebSocket | null>(null);
const altSendIcon = "Send message icon";
/**
 * Clear chat messages and input field
 */
function clearChat() {
  messages.value = [];
  newMessage.value = "";
}

/**
 * Send a message to friend via WebSocket
 */
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

/**
 * Clear chat messages when dialog is opened
 */
watch(() => [props.show, props.friendId], ([newShow, _]) => {
  if (newShow) {
    clearChat();
  }
}, { immediate: true });

/**
 * Close the chat dialog
 */
const closeDialog = () => {
  emit("close");
};

/**
 * Create WebSocket connection when component is mounted
 */
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

/**
 * Close WebSocket connection when component is unmounted
 */
onBeforeUnmount(() => {
  socket.value?.close();
});
</script>

<template>
  <transition name="slide-up">
    <div v-if="show" class="chat-dialog" @click.stop>
      <div class="chat-header">
        <h4>Chat with
          <UsernameText :text="friendId" />
        </h4>
        <button class="close-btn" @click="closeDialog">&times;</button>
      </div>
      <div class="chat-messages">
        <div v-for="(message, index) in messages" :key="index" class="message">
          <strong>{{ message.sender }}:</strong> {{ message.content }}
        </div>
      </div>
      <div class="chat-input">
        <BaseInput v-model="newMessage" placeholder="Type a message..." class="message-container"/>
        <NeutralButton @click="sendMessage" class="send-button">
          <Icon :src="sendIcon" :alt="altSendIcon" class="icon"/>
        </NeutralButton>
      </div>
    </div>
  </transition>
</template>

<style lang="scss" scoped>
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.chat-dialog {
  @include mixins.default-background-styles(global.$bg-color);

  .chat-header {
    @include mixins.default-chat-header-style;

    .close-btn {
      @include mixins.cross-close-button;
    }
  }

  .chat-messages {
    flex-grow: 1;
    overflow-y: auto;
    padding: 5px;
    border-bottom: 1px solid #ddd;

    .message {
      @include mixins.message-style;
    }
  }

  .chat-input {
    @include mixins.default-align-items(1vw);

    .send-button {
      width: 20%;
    }

    .message-container {
      width: 80%;
    }
  }
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: transform 0.3s ease-in-out;
}

.slide-up-enter-from, .slide-up-leave-to {
  transform: translateY(100%);
}
</style>
