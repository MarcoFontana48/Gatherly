<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/utils/auth.js";
import OverlayDialog from "@/components/dialogs/OverlayDialog.vue";
import AcceptButton from "@/components/buttons/AcceptButton.vue";
import BaseInput from "@/components/inputs/BaseInput.vue";
import ErrorText from "@/components/text/ErrorText.vue";
import axios from "axios";
import {validateEmail, wrongEmailFormatString} from "@/utils/validator.ts";
import NeutralButton from "@/components/buttons/NeutralButton.vue";

const props = defineProps<{ showModal: boolean }>();
const emit = defineEmits(["update:showModal"]);
const email = ref("");
const errorMessage = ref("");
const router = useRouter();
const authStore = useAuthStore();
const matchingEmailError = "No matching email found. Please try again.";

async function authenticate() {
  authStore.setAuthToken(email.value);
  emit("update:showModal", false);
  await router.push("/home");
}

const login = async () => {
  console.log("login function called");

  if (!validateEmail(email.value)) {
    errorMessage.value = wrongEmailFormatString;
    return;
  }

  errorMessage.value = "";

  try {
    const response = await axios.get(`http://localhost:8080/users`, {
      params: { email: email.value },
    });

    if (response.status === 200 && response.data) {
      await authenticate();
    } else {
      errorMessage.value = matchingEmailError;
    }
  } catch (error: any) {
    console.error("Error during login request:", error);
    errorMessage.value = error.response.data
  }
};

const signUp = async () => {
  console.log("sign up function called");

  if (!validateEmail(email.value)) {
    errorMessage.value = wrongEmailFormatString;
    return;
  }

  errorMessage.value = "";

  try {
    const response = await axios.post(`http://localhost:8080/users`, {
      email: email.value,
      username: "placeholder",
    });

    if (response.status === 201) {
      await authenticate();
    } else {
      errorMessage.value = matchingEmailError;
    }
  } catch (error: any) {
    console.error("Error during login request:", error);
    errorMessage.value = error.response.data
  }
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
      <span class="buttons">
        <AcceptButton @click="login">Login</AcceptButton>
        <NeutralButton @click="signUp">Sign up</NeutralButton>
      </span>
    </template>
  </OverlayDialog>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.overlay-dialog {
  background: rgba(0, 0, 0, 1);
}

.buttons {
  @include default-align-buttons(1vw);
}
</style>
