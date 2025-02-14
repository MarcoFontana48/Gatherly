<script setup lang="ts">
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
@use "@/styles/mixins" as mixins;
@use "@/styles/global" as global;

.menu-nav {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  position: fixed;
  top: 5%;

  @media (max-width: 768px) {
    flex-direction: row;
    justify-content: space-around;
    width: 100%;
    position: fixed;
    bottom: 0;
    top: auto;
    background-color: global.$bg-color;
    padding: 10px 0;
    box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1);
  }
}

.menu-label {
  @media (max-width: 768px) {
    display: none;
  }
}
</style>
