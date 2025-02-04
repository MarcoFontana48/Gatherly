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
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  z-index: 1000;
}

.side-panel {
  width: 30vw;
  height: 100vh;
  background: white;
  box-shadow: -5px 0 15px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  padding: 20px;
  transform: translateX(100%);
  animation: slide-in 0.3s forwards;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
  margin-bottom: 15px;
}

.panel-header h3 {
  margin: 0;
}

.close-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
}

.side-panel ul {
  list-style: none;
  padding: 0;
}

.side-panel li {
  padding: 10px;
  cursor: pointer;
}

.side-panel li:hover {
  background: #f0f0f0;
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
