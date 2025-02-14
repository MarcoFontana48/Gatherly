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
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.login-page {
  @include mixins.default-text-styles(global.$bg-color);
  @include mixins.default-dialog-style(global.$bg-color);
}
</style>
