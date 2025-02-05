<script setup lang="ts">
import {ref, watch, onMounted, onBeforeUnmount, inject} from "vue";
import axios from "axios";

// Define the 'show' prop using defineProps
const props = defineProps<{
  show: boolean;
}>();

// Define emits to allow emitting events from this component
const emit = defineEmits<{
  (event: 'close'): void;
}>();

// Define a ref to store the friendships and friendship requests
const friendships = ref<any[]>([]);
const friendshipRequests = ref<any[]>([]);
const userId = inject('userId') as string;

// Function to retrieve friendships
const fetchFriendships = async () => {
  try {
    console.log("Fetching friendships of userId: " + userId);
    const response = await axios.get("http://localhost:8081/friends/friendships", { params: { id: userId } });
    console.log("Friendships fetched:", response.data);
    friendships.value = response.data; //! assuming the response contains an array of friendships
    console.log("Friendships:", friendships.value);
  } catch (error) {
    console.error("Failed to fetch friendships:", error);
  }
};

// Function to retrieve friendship requests
const fetchFriendshipRequests = async () => {
  try {
    const response = await axios.get("http://localhost:8081/friends/requests", { params: { id: "test@gmail.com" } });
    console.log("Friendship requests fetched:", response.data);
    friendshipRequests.value = response.data; //! assuming the response contains an array of requests
    console.log("Friendship Requests:", friendshipRequests.value);
  } catch (error) {
    console.error("Failed to fetch friendship requests:", error);
  }
};

// Watch for changes in the 'show' prop
watch(
    () => props.show,
    (newValue) => {
      if (newValue) {
        // Fetch friendships and requests when the menu is opened
        fetchFriendships();
        fetchFriendshipRequests();
      }
    },
    { immediate: true } // Ensures fetching occurs on mount if 'show' is initially true
);

// Close the menu when clicking outside
const closeMenu = () => {
  emit('close');
};

onMounted(() => {
  if (props.show) {
    document.addEventListener('click', closeMenu);
  }
});

onBeforeUnmount(() => {
  document.removeEventListener('click', closeMenu);
});
</script>

<template>
  <div v-if="show" class="overlay" @click.stop="closeMenu">
    <div class="side-panel" @click.stop>
      <div class="panel-header">
        <h3>Friends Menu</h3>
        <button class="close-btn" @click="closeMenu">&times;</button>
      </div>

      <!-- Upper part: Friendship Requests -->
      <div class="upper-section">
        <b>Pending received friendship requests:</b>
        <ul>
          <li v-for="request in friendshipRequests" :key="request.id">{{ request.from.id.value }}</li>
        </ul>
      </div>

      <!-- Separator line -->
      <hr class="separator"/>

      <!-- Lower part: Friendships -->
      <div class="lower-section">
        <b>Your friends:</b>
        <ul>
          <li v-for="friendship in friendships" :key="friendship.id">{{ friendship.id.value }}</li>
        </ul>
      </div>

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
  padding: 5vh;
  max-width: 66vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  @include panel-header-styles;
}

.upper-section, .lower-section {
  margin-top: 5vh;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  margin: 5px 0;
}

.separator {
  margin: 20px 0;
  border: none;
  border-top: 1px solid #ccc; /* Light gray line */
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
