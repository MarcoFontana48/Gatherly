<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
import Feed from "@/components/Feed.vue";
import Friendship from "@/components/Friendship.vue";
</script>

<template>
  <div class="app-container">
    <div class="left-column">
      <RouterLink to="/home" class="link-button">Home</RouterLink>
      <RouterLink to="/profile" class="link-button">Profile</RouterLink>
      <RouterLink to="/settings" class="link-button">Settings</RouterLink>
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

$hover-darken-amount: 40%;
$active-darken-amount: 60%;

.app-container {
  @include default-app-style($bg-color);
}

.left-column {
  @include default-column-style(flex-start);
  @include center-content;
}

.center-column {
  @include default-column-style(center);
}

.right-column {
  @include default-column-style(flex-end);
  @include center-content;
}

.link-button {
  $padding: 10px;
  $font-size: 1.6rem;
  $min-width: 100px;
  $border-radius: 5px;
  $min-size: 1.6rem;
  $max-size: 2rem;
  $border-color: invert($bg-color);
  @include button-styles($padding, $font-size, $min-width, $border-radius, $min-size, $max-size, $border-color);
  @include default-link-styles($bg-color);
  @include center-content;

  &:hover {
    @include adapt-color-based-on-bg($bg-color, $hover-darken-amount, $bg-color);
  }

  &:active {
    @include adapt-color-based-on-bg($bg-color, $active-darken-amount, $bg-color);
  }
}
</style>
