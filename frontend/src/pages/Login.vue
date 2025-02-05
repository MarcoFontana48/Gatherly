<script setup lang="ts">
import { ref } from 'vue';
import LoginDialog from '@/components/dialogs/LoginDialog.vue';

const showModal = ref(true); // Show login dialog automatically

const handleLogin = (email: string) => {
  console.log("User logged in with email:", email);
};
</script>

<template>
  <div class="login-page">
    <LoginDialog v-model:showModal="showModal" @login="handleLogin" />
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.login-page {
  @include default-text-styles($bg-color);
  @include default-dialog-style($bg-color);
}
</style>
