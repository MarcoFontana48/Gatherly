<script setup lang="ts">
import {provide, ref} from "vue";
import MenuButton from "@/components/buttons/NavButton.vue";
import Icon from "@/components/images/Icon.vue";
import FriendshipMenu from "@/components/friendship/FriendshipMenu.vue";
import friendsIcon from "@/assets/friends-solid.svg";
import FriendshipNotificationSection from "@/components/friendship/FriendshipNotificationSection.vue";

const friendshipEvents = ref(["friendship-request-sent", "friendship-request-accepted", "friendship-request-rejected"]);
const showDropdown = ref(false);

provide("friendshipEvents", friendshipEvents.value);

/**
 * Toggles the visibility of the friendship dropdown menu
 */
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};
</script>

<template>
  <div class="friendship-container">
    <MenuButton @click="toggleDropdown">
      Friends
      <Icon :src="friendsIcon" alt="Friends Icon" />
    </MenuButton>
  </div>
  <div class="notifications-container">
    <FriendshipNotificationSection />
  </div>

  <FriendshipMenu :show="showDropdown" @close="showDropdown = false" />
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";

.friendship-container {
  @include display-vertically;
  position: fixed;
  top: 5%;
}

.notifications-container {
  position: fixed;
  top: 20%;
  max-height: 80%;
  overflow-y: auto;
}
</style>