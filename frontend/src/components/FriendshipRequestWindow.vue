<template>
  <div class="friend-request" v-if="showRequest">
    <p class="request-text">
      <UsernameText :text="receiverUsername" />
      <UserIdText :text="receiverId" />
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
import UsernameText from "./UsernameText.vue";
import UserIdText from "./UserIdText.vue";

const receiverId = ref("<user-id>");
const receiverUsername = ref("<username>");
const showRequest = ref(false);

const acceptRequest = () => {
  console.log(`${receiverId.value} accepted`);
};

const denyRequest = () => {
  console.log(`${receiverId.value} denied`);
};

// Listen for SSE events
onMounted(() => {
  const eventSource = new EventSource('http://localhost:8082/notifications');

  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data);
    receiverId.value = data.receiverId;
    receiverUsername.value = data.receiverUsername;
    showRequest.value = true;

    // Hide the request after 10 seconds
    setTimeout(() => {
      showRequest.value = false;
    }, 10_000);
  };
});
</script>

<style lang="scss" scoped>
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

@mixin dynamic-color($bg-color) {
  color: if(lightness($bg-color) > 60%, black, white);
}

@mixin lighten-color($color, $amount) {
  color: lighten($color, $amount);
}

@mixin darken-color($color, $amount) {
  color: darken($color, $amount);
}

@mixin center-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.friend-request {
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

  @mixin button-styles($padding, $font-size, $min-width, $border-radius, $min-size, $max-size, $border-color) {
    padding: $padding;
    border: $border-radius solid $border-color;
    font-size: clamp($min-size, $font-size, $max-size);
    min-width: clamp($min-width, 6vw, $min-size);
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
