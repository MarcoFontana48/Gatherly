<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import MenuNav from "@/components/MenuNav.vue";
import FriendshipSection from "@/components/friendship/FriendshipSection.vue";
import { RouterView } from "vue-router";
import SettingsDialog from "@/components/dialogs/SettingsDialog.vue";

import houseIcon from "@/assets/house-solid.svg";
import userIcon from "@/assets/user-solid.svg";
import cogIcon from "@/assets/cog-solid.svg";

const menuItems = [
  { path: "/home", label: "Home", icon: houseIcon },
  { path: "/profile", label: "Profile", icon: userIcon },
  { path: "/settings", label: "Settings", icon: cogIcon },
];

const showModal = ref(false);
const toggleModal = () => {
  showModal.value = !showModal.value;
};

const route = useRoute();
</script>

<template>
  <div class="app-container">
    <div v-if="route.path !== '/login'" class="sidebar left-column">
      <MenuNav :menuItems="menuItems" @openModal="toggleModal" />
    </div>

    <div class="center-column">
      <RouterView />
    </div>

    <div v-if="route.path !== '/login'" class="sidebar right-column">
      <FriendshipSection />
    </div>

    <div class="settings-dialog-container">
      <SettingsDialog :showModal="showModal" @update:showModal="showModal = $event" />
    </div>

    <div v-if="route.path !== '/login'" class="mobile-nav">
      <MenuNav :menuItems="menuItems" @openModal="toggleModal" />
      <FriendshipSection />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.app-container {
  display: flex;
  height: 100vh;
  @include mixins.default-app-style(global.$bg-color);

  @media (max-width: global.$mobile-screen-size) {
    flex-direction: column;
    height: auto;
  }

  .left-column, .right-column {
    width: 20%;
    padding: 2.5vw;
    flex: none;

    @media (max-width: global.$mobile-screen-size) {
      display: none;
    }
  }

  .center-column {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    overflow-y: auto;

    @media (max-width: global.$mobile-screen-size) {
      width: 100%;
    }
  }

  .mobile-nav {
    display: none;

    @media (max-width: global.$mobile-screen-size) {
      display: flex;
      justify-content: space-around;
      align-items: center;
      position: fixed;
      bottom: 0;
      width: 100%;
      background-color: global.$bg-color;
      padding: 10px 0;
      box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1);
    }
  }

  .settings-dialog-container {
    max-width: 20%;
  }
}

</style>
