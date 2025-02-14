<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { io, Socket } from 'socket.io-client';
import BaseInput from "@/components/inputs/BaseInput.vue";
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import Icon from "@/components/images/Icon.vue";
import sendIcon from "@/assets/paper-plane-solid.svg";

const props = defineProps<{
  show: boolean;
  id: string;
}>();
const emit = defineEmits<{ (event: 'close'): void }>();

const messages = ref<{ content: string }[]>([]);
const newMessage = ref('');
const altSendIcon = 'Send message icon';

let socket: Socket;

/**
 * Joins the chat room via WebSocket and listens for new messages
 */
onMounted(() => {
  socket = io('http://localhost:8082');

  console.log('Joining room:', props.id);
  socket.emit('joinRoom', props.id);

  socket.on('newMessage', (messageData: { content: string }) => {
    console.log("Received message '" + messageData.content + "' from socket on chat room '", props.id);
    messages.value.push(messageData);
  });
});

/**
 * Disconnects from the WebSocket when the dialog is closed
 */
onBeforeUnmount(() => {
  if (socket) {
    socket.disconnect();
    console.log('Disconnected from socket');
  }
});

/**
 * Send a message to the chat room via WebSocket
 */
const sendMessage = () => {
  if (!newMessage.value.trim()) return;

  const messageData = {
    content: newMessage.value,
  };

  console.log('Sending message:', messageData);

  socket.emit('sendMessage', props.id, messageData);

  newMessage.value = '';
};

/**
 * Close the chat dialog
 */
const closeDialog = () => {
  emit('close');
};
</script>

<template>
  <div v-if="show" class="post-chat-dialog">
    <div class="chat-header">
      <h5>anonymous global chat for this post</h5>
      <button class="close-btn" @click="closeDialog">&times;</button>
    </div>
    <div class="chat-messages">
      <div v-for="(message, index) in messages" :key="index" class="message">
        {{ message.content }}
      </div>
    </div>
    <div class="chat-input">
      <BaseInput v-model="newMessage" placeholder="Type a message..." />
      <NeutralButton @click="sendMessage" class="send-button">
        <Icon :src="sendIcon" :alt="altSendIcon" class="icon"/>
      </NeutralButton>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.post-chat-dialog {
  @include mixins.over-background-post-style(global.$bg-color);

  .chat-header {
    @include mixins.default-chat-header-style;

    .close-btn {
      @include mixins.cross-close-button;
    }
  }

  .chat-messages {
    max-height: 150px;
    overflow-y: auto;
    margin-bottom: 10px;

    .message {
      @include mixins.message-style;
    }
  }

  .chat-input {
    display: flex;

    .send-button {
      width: 5%;

      .icon {
        width: 1.5rem;
        height: 1.5rem;
      }
    }
  }
}
</style>