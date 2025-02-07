<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import UsernameText from "@/components/text/UsernameText.vue";
import ErrorText from "@/components/text/ErrorText.vue";
import axios from 'axios';

const email = sessionStorage.getItem('authToken');
const errorMessage = "No email found";

// Ref to store fetched posts
const posts = ref<any[]>([]);

const fetchPosts = async () => {
  if (email) {
    try {
      console.log('Fetching posts of user:', email);

      const response = await axios.get('http://localhost:8082/contents/posts/', { params: { userID: email } });

      console.log('Posts:', response.data);

      posts.value = response.data;
    } catch (error) {
      console.error('Error fetching posts:', error);
    }
  } else {
    console.error("Email not found");
  }
};

// Fetch posts when component is mounted
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

    <!-- Display the posts -->
    <div v-if="posts.length">
      <div v-for="post in posts" :key="post.g_1.e_1">
        <p>{{ post.content }}</p>
        <p>Author: {{ post.author.name }}</p>
      </div>
    </div>
    <p v-else>No posts available</p>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.profile-container {
  @include default-text-styles($bg-color);
}
</style>
