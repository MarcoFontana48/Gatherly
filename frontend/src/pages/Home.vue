<script setup lang="ts">
import { ref } from 'vue';
import Post from '@/components/feed/Post.vue';
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import plusIcon from "@/assets/plus-solid.svg";
import Icon from "@/components/images/Icon.vue";
import PostDialog from '@/components/dialogs/PostDialog.vue';

const altAddIcon = 'Add post icon';

type PostType = {
  id: number;
  username: string;
  content: string;
  timestamp: string;
};

const posts = ref<PostType[]>([]);
const showDialog = ref(false);
const showContentDialog = ref(false);
const newPostContent = ref('');

const openContentDialog = () => {
  showDialog.value = false;
  showContentDialog.value = true;
};

const addPost = () => {
  if (newPostContent.value.trim()) {
    posts.value.push({
      id: posts.value.length + 1,
      username: 'User',
      content: newPostContent.value,
      timestamp: new Date().toLocaleString(),
    });
  }
  showContentDialog.value = false;
  newPostContent.value = '';
};

const toggleDialogs = () => {
  if (showDialog.value || showContentDialog.value) {
    showDialog.value = false;
    showContentDialog.value = false;
  } else {
    showDialog.value = true;
  }
};
</script>

<template>
  <div class="feed-container">
    <div v-if="posts.length" class="post-list">
      <Post v-for="post in posts" :key="post.id" :content="post.content" />
    </div>
    <p v-else>No posts available</p>
  </div>
  <div class="button-container">
    <NeutralButton @click="toggleDialogs">
      <Icon :src="plusIcon" :alt="altAddIcon" />
    </NeutralButton>
  </div>

  <PostDialog :show="showDialog" title="Add a new post" body-text="Do you want to add a new post?" @update:show="showDialog = $event" @confirm="openContentDialog" />
  <PostDialog :show="showContentDialog" title="Write your post" input v-model="newPostContent" @update:show="showContentDialog = $event" @confirm="addPost" />
</template>

<style lang="scss" scoped>
@import "@/styles/mixins";
@import "@/styles/global";

.feed-container {
  @include default-text-styles($bg-color);
  width: 95%;
  margin: auto;
  padding: 1%;
  border-radius: 0.5%;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.button-container {
  position: fixed;
  bottom: 5%;
  width: 5%;
}
</style>