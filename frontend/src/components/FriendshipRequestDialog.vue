<template>
  <div class="friend-request" v-if="showRequest">
    <p class="request-text">
      <UserIdText :text="senderId" />
      wants to add you as a friend
    </p>
    <div class="buttons">
      <button @click="acceptRequest" class="accept">Accept</button>
      <button @click="denyRequest" class="deny">Deny</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import UserIdText from "./UserIdText.vue";

const senderId = ref("<sender-id>");
const showRequest = ref(false);

const acceptRequest = () => {
  console.log(`${senderId.value} accepted`);
};

const denyRequest = () => {
  console.log(`${senderId.value} denied`);
};

// Listen for SSE events
onMounted(() => {
  console.log("mounted")
  const eventSource = new EventSource('http://localhost:8081/notifications?id=test');

  eventSource.onmessage = (event) => {
    console.log('Received event:', event.data);

    const data = JSON.parse(event.data);
    senderId.value = data.sender;
    showRequest.value = true;

    // Hide the request after 10 seconds
    setTimeout(() => {
      showRequest.value = false;
    }, 10_000);
  };
});
</script>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
$friendship-request-window-bg-color: #f9f9f9;
$border-color: #1a1a1a;
$accept-color: #5cb85c;
$deny-color: #d9534f;

$padding: 2vw;
$font-size-base: 1vw;
$font-size-min: 1rem;
$font-size-max: 2.5rem;
$min-button-width: 6vw;
$min-friendship-request-window-width: 10vw;
$max-friendship-request-window-width: 20vw;
$button-padding: 1vw;
$border-radius: 0.2vw;
$gap: 1vw;

.friend-request {
  @include default-text-styles;

  padding: $padding;
  border: $border-radius solid $border-color;
  background-color: $friendship-request-window-bg-color;
  min-width: $min-friendship-request-window-width;
  max-width: $max-friendship-request-window-width;
  text-align: center;

  .request-text {
    @include dynamic-color($friendship-request-window-bg-color);
    font-size: clamp($font-size-min, $font-size-base, $font-size-max);
  }

  .buttons {
    margin-top: $gap;
    display: flex;
    justify-content: center;
    gap: $gap;
  }

  button {
    @include center-content;
    @include button-styles($button-padding, $font-size-base, $min-button-width, $border-radius, $font-size-min, $font-size-max, $border-color);

    &.accept {
      background-color: $accept-color;
      @include dynamic-color($accept-color);

      &:hover {
        @include lighten-color($accept-color, 30%);
      }

      &:active {
        @include darken-color($accept-color, 90%);
      }
    }

    &.deny {
      background-color: $deny-color;
      @include dynamic-color($deny-color);

      &:hover {
        @include lighten-color($deny-color, 30%);
      }

      &:active {
        @include darken-color($deny-color, 90%);
      }
    }
  }
}
</style>
