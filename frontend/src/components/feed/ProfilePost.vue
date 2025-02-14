<script setup lang="ts">
import axios from 'axios';
import Post from '@/components/feed/Post.vue';
import DeclineButton from "@/components/buttons/DeclineButton.vue";
import Icon from "@/components/images/Icon.vue";
import removePostIcon from "@/assets/trash-solid.svg";

const altRemovePostIcon = "remove post icon";

const props = defineProps<{
  content: string,
  id: string,
  author: string,
}>();

const emit = defineEmits(['postDeleted']);

/**
 * Deletes the post from the database sending a DELETE request to the server
 */
const handleDelete = async () => {
  try {
    await axios.delete(`http://localhost:8082/contents/posts/${props.author}/${props.id}`);
    console.log(`Post ${props.id} deleted successfully`);
    emit('postDeleted');
  } catch (error) {
    console.error(`Error deleting post ${props.id}:`, error);
  }
};
</script>

<template>
  <div class="profile-post-container">
    <Post :content="props.content" :id="props.id" :author="props.author" />
    <div class="post-buttons">
      <DeclineButton @click="handleDelete">
        <Icon :src="removePostIcon" :alt="altRemovePostIcon" class="icon"/>
      </DeclineButton>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.profile-post-container {
  @include mixins.default-post-style(global.$bg-color, 95%);
  @include mixins.display-vertically;
  margin-bottom: 1rem;

  .post-buttons {
    margin-top: 1%;
    gap: 1%;
  }
}
</style>