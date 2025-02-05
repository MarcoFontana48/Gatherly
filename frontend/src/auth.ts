import { defineStore } from "pinia";
import { ref, computed } from "vue";

export const useAuthStore = defineStore("auth", () => {
    const authToken = ref(sessionStorage.getItem("authToken") || null);

    const isAuthenticated = computed(() => authToken.value !== null);

    function setAuthToken(token: string) {
        authToken.value = token;
        sessionStorage.setItem("authToken", token);
    }

    function clearAuthToken() {
        authToken.value = null;
        sessionStorage.removeItem("authToken");
    }

    return { authToken, isAuthenticated, setAuthToken, clearAuthToken };
});
