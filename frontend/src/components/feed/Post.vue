<script setup lang="ts">
import { ref } from 'vue';
import UsernameText from '@/components/text/UsernameText.vue';
import PostText from '@/components/text/PostText.vue';
import PostChatDialog from '@/components/dialogs/PostChatDialog.vue';
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import Icon from "@/components/images/Icon.vue";
import chatIcon from "@/assets/message-solid.svg";

const altChatIcon = 'Chat icon';
const showChatDialog = ref(false);

const props = defineProps<{
  content: string,
  id: string,
  author: string,
}>();
</script>

<template>
  <div class="post-container">
    <div class="post-content-container">
      <UsernameText :text="props.author" />
      <PostText :content="props.content" />
    </div>
    <div class="post-buttons">
      <NeutralButton @click="showChatDialog = !showChatDialog">
        <Icon :src="chatIcon" :alt="altChatIcon" class="icon"/>
      </NeutralButton>
    </div>
    <div class="post-chat-dialog-container">
      <PostChatDialog :show="showChatDialog" @close="showChatDialog = false" :id="props.id" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.post-container {
  @include mixins.default-post-style(cyan, 85%);
  @include mixins.display-vertically;

  .post-content-container {
    @include mixins.over-background-post-style(global.$bg-color);

  }

  .post-buttons {
    margin-top: 1%;
    gap: 1%;
    width: 2%;

    .icon {
      @media (max-width: global.$mobile-screen-size) {
        width: 1.2rem;
        height: 1.2rem;
      }
      @media (min-width: global.$mobile-screen-size) {
        width: 1.7rem;
        height: 1.7rem;
      }
    }
  }

  .post-chat-dialog-container {
    padding: 1%;
  }
}

</style>