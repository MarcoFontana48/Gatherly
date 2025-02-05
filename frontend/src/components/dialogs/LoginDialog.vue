<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/auth.ts"; // Import Pinia store
import OverlayDialog from "@/components/dialogs/OverlayDialog.vue";
import AcceptButton from "@/components/buttons/AcceptButton.vue";
import BaseInput from "@/components/inputs/BaseInput.vue";
import ErrorText from "@/components/text/ErrorText.vue";

const props = defineProps<{ showModal: boolean }>();
const emit = defineEmits(["update:showModal"]);

const email = ref("");
const errorMessage = ref("");
const router = useRouter();
const authStore = useAuthStore(); // Use Pinia store

import axios from "axios"; // Import axios

const login = async () => {
  console.log("login function called");

  if (!validateEmail(email.value)) {
    errorMessage.value =
        "The email you have inserted is not formatted properly.\nA valid email should be in the format:\nemail@something.domain";
    return;
  }

  errorMessage.value = "";

  try {
    // checks if user exists
    const response = await axios.get(`http://localhost:8080/users`, {
      params: { email: email.value },
    });

    if (response.status === 200 && response.data) {
      authStore.setAuthToken(email.value);
      emit("update:showModal", false);
      await router.push("/home");
    } else {
      errorMessage.value = "No matching email found. Please try again.";
    }
  } catch (error: any) {
    switch (error.response?.status) {
      case 404:
        errorMessage.value = "No matching email found. Please try again.";
        break;
      case 400:
        errorMessage.value = "The email you have inserted is not formatted properly.\nA valid email should be in the format:\nemail@something.domain";
        break;
      default:
        errorMessage.value = "An error occurred. Please try again later.";
    }
    console.error("Error during login request:", error);
  }
};


const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};
</script>

<template>
  <OverlayDialog :showModal="props.showModal" @update:showModal="emit('update:showModal', $event)" class="overlay-dialog">
    <template #header>
      <h3>Social-Network</h3>
    </template>
    <template #body>
      <p>Enter your credentials below to login</p>
      <BaseInput v-model="email" type="email" placeholder="Enter your email" />
      <p>
        <ErrorText v-if="errorMessage" :text="errorMessage" class="error-message" />
      </p>
    </template>
    <template #footer>
      <AcceptButton @click="login">Login</AcceptButton>
    </template>
  </OverlayDialog>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.overlay-dialog {
  background: rgba(0, 0, 0, 1);
}
</style>
