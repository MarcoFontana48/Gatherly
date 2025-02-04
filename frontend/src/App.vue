<script setup lang="ts">
import { ref } from 'vue';
import MenuNav from "@/components/MenuNav.vue";
import Friendship from "@/components/section/FriendshipSection.vue";
import { RouterView } from "vue-router";
import SettingsDialog from "@/components/SettingsDialog.vue";

import houseIcon from "@/assets/house-solid.svg";
import userIcon from "@/assets/user-solid.svg";
import cogIcon from "@/assets/cog-solid.svg";

// Define menu items
const menuItems = [
  { path: "/home", label: "Home", icon: houseIcon },
  { path: "/profile", label: "Profile", icon: userIcon },
  { path: "/settings", label: "Settings", icon: cogIcon },
];

// State to control modal visibility
const showModal = ref(false);

// Method to toggle modal visibility
const toggleModal = () => {
  showModal.value = !showModal.value;
};
</script>

<template>
  <div class="app-container">
    <div class="left-column">
      <MenuNav :menuItems="menuItems" @openModal="toggleModal"/>
    </div>
    <div class="center-column">
      <RouterView/>
    </div>
    <div class="right-column">
      <Friendship/>
    </div>

    <!-- SettingsDialog Component -->
    <SettingsDialog :showModal="showModal" @update:showModal="showModal = $event" />
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.app-container {
  @include default-app-style($bg-color);
}

.left-column {
  @include default-column-style(flex-start);
  @include align-to(flex-start);
}

.center-column {
  @include default-column-style(center);
}

.right-column {
  @include default-column-style(flex-end);
  @include align-to(flex-end);
}
</style>
