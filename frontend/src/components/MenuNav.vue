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
        <Icon :src="item.icon" :alt="`${item.label} Icon`" />{{ item.label }}
      </MenuButton>
    </RouterLink>
  </nav>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";

.menu-nav {
  @include display-vertically;
  position: fixed;
  top: 5%;
}
</style>
