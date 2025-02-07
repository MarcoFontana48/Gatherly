<script setup lang="ts">
import { ref } from 'vue';
import Post from '@/components/feed/Post.vue';
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import plusIcon from "@/assets/plus-solid.svg";
import Icon from "@/components/images/Icon.vue";

const altAddIcon = 'Add post icon';

type PostType = {
  id: number;
  username: string;
  content: string;
  timestamp: string;
};

const posts = ref<PostType[]>([]);

const addPost = () => {
  posts.value.push({
    id: posts.value.length + 1,
    username: 'User',
    content: 'This is a new post!',
    timestamp: new Date().toLocaleString(),
  });
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
    <NeutralButton @click="addPost">
      <Icon :src="plusIcon" :alt="altAddIcon" />
    </NeutralButton>
  </div>
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

.add-post-btn {
  margin-bottom: 16px;
  padding: 8px 12px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-post-btn:hover {
  background-color: #0056b3;
}

.button-container {
  position: fixed;
  bottom: 5%;
  width: 5%;
}
</style>