<script setup lang="ts">
import {ref, onMounted, onUnmounted, computed} from 'vue';
import Post from '@/components/feed/Post.vue';
import NeutralButton from "@/components/buttons/NeutralButton.vue";
import plusIcon from "@/assets/plus-solid.svg";
import Icon from "@/components/images/Icon.vue";
import PostDialog from '@/components/dialogs/PostDialog.vue';
import {useAuthStore} from "@/utils/auth.ts";
import {defineSseEventSource} from "@/utils/sse.ts";
import axios from 'axios';

const altAddIcon = 'Add post icon';
const authStore = useAuthStore();
const email = computed(() => authStore.authToken);

type PostType = {
  id: string,
  author: {
    email: string,
    name: string,
  },
  content: string,
};

const posts = ref<PostType[]>([]);
const showDialog = ref(false);
const showContentDialog = ref(false);
const newPostContent = ref('');

const openContentDialog = () => {
  showDialog.value = false;
  showContentDialog.value = true;
};

let eventSource: EventSource | null = null;

/**
 * Fetches posts from the server sending a GET request to the server
 */
const fetchPosts = async () => {
  try {
    console.log('Fetching posts for user:', email.value);
    const response = await axios.get(`http://localhost:8082/contents/posts/feed/${email.value}`);
    console.log('Posts:', response.data);

    if (Array.isArray(response.data.posts)) {
      posts.value = response.data.posts;
    } else {
      console.error('Unexpected response structure:', response.data);
      posts.value = [];
    }
  } catch (error) {
    console.error('Error fetching posts:', error);
  }
};

/**
 * Adds a new post to the server sending a POST request
 */
const addPost = async () => {
  if (newPostContent.value.trim()) {
    const postData = {
      user: {
        email: email.value,
        name: "placeholder",
      },
      content: newPostContent.value,
    };

    console.log('Adding post:', postData);

    try {
      const response = await axios.post('http://localhost:8082/contents/posts', postData, {
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log("response:", response);

    } catch (error) {
      console.error('Error adding post:', error);
    }
  }
  showContentDialog.value = false;
  newPostContent.value = '';
};

/**
 * Toggles the visibility of the dialogs
 */
const toggleDialogs = () => {
  if (showDialog.value || showContentDialog.value) {
    showDialog.value = false;
    showContentDialog.value = false;
  } else {
    showDialog.value = true;
  }
};

/**
 * Starts the SSE connection to listen for new posts
 */
const startSSE = () => {
  eventSource = defineSseEventSource(email.value, 'localhost', '8082');

  if (eventSource) eventSource.onmessage = (event: MessageEvent<any>) => {
    console.log('Received event:', event.data);

    try {
      const newPost: PostType = JSON.parse(event.data);
      console.log('New post:', newPost);

      if (newPost.author && newPost.author.email) {
        posts.value.push(newPost);
      } else {
        console.error('Post received without valid user information');
      }
    } catch (error) {
      console.error('Error parsing SSE data:', error);
    }
  };
};

/**
 * Closes the SSE connection when the component is unmounted
 */
onUnmounted(() => {
  if (eventSource) {
    eventSource.close();
    console.log('EventSource closed');
  }
});

/**
 * Fetch posts and start SSE connection when component is mounted
 */
onMounted(() => {
  fetchPosts();
  startSSE();
});
</script>

<template>
  <div class="feed-container">
    <div v-if="posts.length" class="post-list">
      <Post v-for="post in posts" :content="post.content" :id="post.id" :author="post.author.email" />
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

  .post-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}

.button-container {
  position: fixed;
  bottom: 5%;
}
</style>
