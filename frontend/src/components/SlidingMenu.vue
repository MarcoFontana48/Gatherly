<script setup lang="ts">
defineProps<{
  show: boolean;
}>();

const emit = defineEmits(["close"]);
</script>

<template>
  <div v-if="show" class="overlay" @click.self="emit('close')">
    <div class="side-panel">
      <div class="panel-header">
        <h3>Friends Menu</h3>
        <button class="close-btn" @click="emit('close')">&times;</button>
      </div>
      <ul>
        <li><a href="#">Friendship Requests</a></li>
        <li><a href="#">All Friendships</a></li>
      </ul>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/mixins.scss";
@import "@/styles/global.scss";

.overlay {
  @include overlay-background;
  @include default-text-styles($bg-color);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
}

.side-panel {
  @include side-panel($bg-color);
}

.panel-header {
  @include panel-header-styles;
}

.side-panel {
  @include side-panel-list-styles;
}

.close-btn {
  @include default-close-btn-style;
}

@keyframes slide-in {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}
</style>
