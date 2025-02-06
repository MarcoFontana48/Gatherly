<script setup lang="ts">
import {provide, ref} from "vue";
import MenuButton from "@/components/buttons/NavButton.vue";
import Icon from "@/components/images/Icon.vue";
import FriendshipMenu from "@/components/FriendshipMenu.vue";
import friendsIcon from "@/assets/friends-solid.svg";
import FriendshipRequestDialog from "@/components/dialogs/FriendshipRequestDialog.vue";

const friendshipEvents = ref(["friendship-request-sent", "friendship-request-accepted", "friendship-request-rejected"]);
const showDropdown = ref(false);

provide("friendshipEvents", friendshipEvents.value);

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

  <FriendshipRequestDialog></FriendshipRequestDialog>

  <FriendshipMenu :show="showDropdown" @close="showDropdown = false" />
</template>

<style lang="scss" scoped>
.friendship-container {
  display: inline-block;
}
</style>
