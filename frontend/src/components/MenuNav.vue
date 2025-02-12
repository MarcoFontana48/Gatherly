<script setup lang="ts">
import { defineProps, defineEmits } from "vue";
import MenuButton from "@/components/buttons/NavButton.vue";
import Icon from "@/components/images/Icon.vue";

defineProps<{
  menuItems: { path: string; label: string; icon: string }[];
}>();

const emit = defineEmits(['openModal']);
</script>

<template>
  <nav class="menu-nav">
    <RouterLink v-for="item in menuItems" :key="item.path" :to="item.path">
      <MenuButton @click="item.label === 'Settings' && emit('openModal')">
        <Icon :src="item.icon" :alt="`${item.label} Icon`" />
        <span class="menu-label">{{ item.label }}</span>
      </MenuButton>
    </RouterLink>
  </nav>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.menu-nav {
  display: flex;
  flex-direction: column; // Default (Desktop)
  align-items: center;
  gap: 1rem;
  position: fixed;
  top: 5%;

  @media (max-width: 768px) {
    flex-direction: row; // Horizontal layout on mobile
    justify-content: space-around;
    width: 100%;
    position: fixed;
    bottom: 0; // Fix at the bottom
    top: auto;
    background-color: $bg-color;
    padding: 10px 0;
    box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1);
  }
}

/* Hide labels on mobile */
.menu-label {
  @media (max-width: 768px) {
    display: none;
  }
}
</style>
