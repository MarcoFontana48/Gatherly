<script setup lang="ts">
import { ref, onMounted } from 'vue';
import UsernameText from '@/components/text/UsernameText.vue';
import ErrorText from '@/components/text/ErrorText.vue';
import axios from 'axios';
import ProfilePost from "@/components/feed/ProfilePost.vue";

const email = sessionStorage.getItem('authToken');
const errorMessage = 'No email found';

// Ref to store fetched posts
const posts = ref<any[]>([]);

const fetchPosts = async () => {
  if (email) {
    try {
      console.log('Fetching posts of user:', email);

      const response = await axios.get(`http://localhost:8082/contents/posts/${email}`);

      console.log('Posts:', response.data);

      posts.value = response.data || [];
    } catch (error) {
      console.error('Error fetching posts:', error);
      posts.value = [];
    }
  } else {
    console.error('Email not found');
    posts.value = [];
  }
};

onMounted(() => {
  fetchPosts();
});
</script>

<template>
  <div class="profile-container">
    <p v-if="email">
      Your email: <UsernameText :text="email" />
    </p>
    <p v-else>
      <ErrorText :text="errorMessage" />
    </p>
    <p v-if="posts.length != 1">
      You currently have {{ posts.length }} posts:
    </p>
    <p v-else>
      You currently have 1 post:
    </p>
    <div v-if="posts && posts.length" class="post-list">
      <ProfilePost v-for="post in posts" :content="post.content" :id="post.id" :author="email" @postDeleted="fetchPosts" />
    </div>
    <p v-else>No posts available</p>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/mixins.scss';
@import '@/styles/global.scss';

.profile-container {
  @include default-text-styles($bg-color);
  width: 95%;
  margin: auto;
  padding: 1%;
  border-radius: 0.5%;

  .post-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}
</style>