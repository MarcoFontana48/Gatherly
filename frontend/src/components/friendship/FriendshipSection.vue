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
    <MenuButton @click="toggleDropdown" class="friendship-button">
      <Icon :src="friendsIcon" alt="Friends Icon" />
      <span class="menu-label">Friends</span>
    </MenuButton>
  </div>

  <div class="notifications-container">
    <FriendshipNotificationSection />
  </div>

  <FriendshipMenu :show="showDropdown" @close="showDropdown = false" />
</template>

<style lang="scss" scoped>
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.friendship-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  top: 5%;

  @media (max-width: global.$mobile-screen-size) {
    flex-direction: row;
    justify-content: center;
    position: fixed;
    top: 0;
    right: 0;
    background-color: global.$bg-color;
    padding: 10px 15px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  }

  .friendship-button {
    @media (max-width: global.$mobile-screen-size) {
      margin-left: auto;
    }

    .menu-label {
      @media (max-width: global.$mobile-screen-size) {
        display: none;
      }
    }
  }
}

.notifications-container {
  position: fixed;
  top: 20%;
  max-height: 80%;
  overflow-y: auto;
}
</style>
