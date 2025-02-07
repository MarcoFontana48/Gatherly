<script setup lang="ts">
import { ref, defineProps, defineEmits } from 'vue';
import BaseInput from "@/components/inputs/BaseInput.vue";
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import Icon from "@/components/images/Icon.vue";
import sendIcon from "@/assets/paper-plane-solid.svg";

const props = defineProps<{ show: boolean }>();
const emit = defineEmits<{ (event: 'close'): void }>();
const messages = ref<{ sender: string; content: string }[]>([]);
const newMessage = ref('');
const altSendIcon = 'Send message icon';

const sendMessage = () => {
  if (!newMessage.value.trim()) return;

  const messageData = {
    sender: 'You',
    content: newMessage.value,
  };

  messages.value.push(messageData);
  newMessage.value = '';
};

const closeDialog = () => {
  emit('close');
};
</script>

<template>
  <div v-if="show" class="post-chat-dialog">
    <div class="chat-header">
      <h4>Global anonymous chat</h4>
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
@import "@/styles/mixins";
@import "@/styles/global";

.post-chat-dialog {
  @include over-background-post-style($bg-color);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
}

.chat-messages {
  max-height: 150px;
  overflow-y: auto;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 5px;
}

.chat-input {
  display: flex;
  gap: 5px;

  .send-button {
    width: 5%;

    .icon {
      width: 1.5rem;
      height: 1.5rem;
    }
  }
}

.chat-input input {
  flex-grow: 1;
  padding: 1%;
}

.chat-input button {
  padding: 5px 10px;
}
</style>