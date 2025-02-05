<script setup lang="ts">
import UsernameText from "@/components/text/UsernameText.vue";
import ErrorText from "@/components/text/ErrorText.vue";

const email = sessionStorage.getItem('authToken');
const errorMessage = "No email found";
</script>

<template>
  <div class="profile-container">
    <p v-if="email">
      Your email: <UsernameText :text="email" />
    </p>

    <p v-else>
      <ErrorText :text="errorMessage" />
    </p>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.profile-container {
  @include default-text-styles($bg-color);
}
</style>
