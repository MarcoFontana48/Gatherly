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
defineProps<{ content: string }>();
</script>

<template>
  <div class="post-container">
    <div class="post-content-container">
      <UsernameText text="Placeholder username" />
      <PostText :content="content" />
    </div>
    <div class="post-buttons">
      <NeutralButton @click="showChatDialog = !showChatDialog">
        <Icon :src="chatIcon" :alt="altChatIcon" class="icon"/>
      </NeutralButton>
    </div>
    <div>
      <PostChatDialog :show="showChatDialog" friend-id="111" @close="showChatDialog = false" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins";
@import "@/styles/global";

.post-container {
  @include default-post-style($bg-color, 95%);
  @include display-vertically;

  .post-content-container {
    @include over-background-post-style($bg-color);

  }

  .post-buttons {
    margin-top: 1%;
    gap: 1%;
    width: 2%;

    .icon {
      width: 1.5rem;
      height: 1.5rem;
    }
  }
}

</style>