<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
import Feed from "@/components/Feed.vue";
import Friendship from "@/components/Friendship.vue";
import BaseButton from "@/components/buttons/BaseButton.vue";
import RouterLinkButton from "@/components/buttons/PagesButton.vue";
</script>

<template>
  <div class="app-container">
    <div class="left-column">
      <RouterLink to="/home" class="link-button">
        <RouterLinkButton>Home</RouterLinkButton>
      </RouterLink>

      <RouterLink to="/profile" class="link-button">
        <RouterLinkButton>Profile</RouterLinkButton>
      </RouterLink>

      <RouterLink to="/settings" class="link-button">
        <RouterLinkButton>Settings</RouterLinkButton>
      </RouterLink>

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
@import "@/styles/global.scss";

.app-container {
  @include default-app-style($bg-color);
}

.left-column {
  @include default-column-style(flex-start);
  @include align-to(flex-start);
}

.center-column {
  @include default-column-style(center);
}

.right-column {
  @include default-column-style(flex-end);
  @include align-to(center);
}
</style>
