<script setup lang="ts">
import {ref, defineProps, defineEmits, provide} from 'vue';
import { useRouter } from 'vue-router';
import OverlayDialog from '@/components/dialogs/OverlayDialog.vue';
import AcceptButton from '@/components/buttons/AcceptButton.vue';
import BaseInput from '@/components/inputs/BaseInput.vue';
import ErrorText from '@/components/text/ErrorText.vue';

const props = defineProps<{ showModal: boolean }>();
const emit = defineEmits(['update:showModal', 'login']);

const email = ref("");
const errorMessage = ref("");

const router = useRouter();

const login = () => {
  if (!validateEmail(email.value)) {
    errorMessage.value = "The email you have inserted is not formatted properly.\nA valid email should be in the format:\nemail@something.domain";
    return;
  }

  errorMessage.value = "";
  emit('login', email.value);
  emit('update:showModal', false);

  // since this is a simple app prototype, we can store the email as a token, in a real app we would use a more secure
  // token, like a JWT token.
  localStorage.setItem('authToken', email.value);

  provide('userId', email.value);

  router.push('/home');
};

const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};
</script>

<template>
  <OverlayDialog v-model:showModal="props.showModal" class="overlay-dialog">
    <template #header>
      <h3>Social-Network</h3>
    </template>

    <template #body>
      <p>Enter your credentials below to login</p>
      <BaseInput v-model="email" type="email" placeholder="Enter your email" />
      <p>
        <ErrorText v-if="errorMessage" :text="errorMessage" class="error-message"/>
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
