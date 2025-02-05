<script setup lang="ts">
import { ref } from "vue";
import MenuButton from "@/components/buttons/NavButton.vue";
import Icon from "@/components/images/Icon.vue";
import FriendshipMenu from "@/components/FriendshipMenu.vue";
import friendsIcon from "@/assets/friends-solid.svg";

const showDropdown = ref(false);

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

  <FriendshipMenu :show="showDropdown" @close="showDropdown = false" />
</template>

<style lang="scss" scoped>
.friendship-container {
  display: inline-block;
}
</style>
