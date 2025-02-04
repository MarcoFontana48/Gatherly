<script setup lang="ts">
import Feed from "@/components/Feed.vue";
import Friendship from "@/components/Friendship.vue";
import {RouterView} from "vue-router";
</script>

<template>
  <div class="app-container">
    <div class="left-column">
      <RouterLink to="/settings">Settings</RouterLink>
      <RouterLink to="/profile">Profile</RouterLink>
      <RouterLink to="/home">Home</RouterLink>
      <RouterView /> <!-- routes render only in the left column, leaving the center and right columns untouched -->
    </div>
    <div class="center-column">
      <Feed />
    </div>
    <div class="right-column">
      <Friendship />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";

.app-container {
  @include default-app-style;
}

.left-column,
.center-column,
.right-column {
  @include default-app-style;
  @include default-column-style;
}
</style>
