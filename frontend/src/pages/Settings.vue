<script setup lang="ts">
import { ref } from "vue";

const isDarkMode = ref(false); // Dark mode state

// Toggle dark mode on button click
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value;
  // Optionally store in localStorage to persist dark mode across sessions
  localStorage.setItem("darkMode", isDarkMode.value.toString());
};

// Check localStorage to set the initial dark mode state
if (localStorage.getItem("darkMode") === "true") {
  isDarkMode.value = true;
}
</script>

<template>
  <div :class="{ 'dark-mode': isDarkMode }" class="settings-container">
    <p>Settings page</p>
    <!-- Button to toggle dark mode -->
    <button @click="toggleDarkMode">
      Toggle Dark Mode
    </button>
  </div>
</template>

<style lang="scss" scoped>
/* Define light mode styles */
.settings-container {
  background-color: white;
  color: black;
  padding: 20px;
  border-radius: 10px;
}

/* Dark mode styles */
.dark-mode {
  background-color: #121212;
  color: white;
}

button {
  background-color: #008cba;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:hover {
  background-color: #005f73;
}
</style>
