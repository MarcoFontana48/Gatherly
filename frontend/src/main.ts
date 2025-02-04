import { createRouter, createWebHistory } from 'vue-router';
import Settings from './pages/Settings.vue';
import Profile from './pages/Profile.vue';
import { createApp } from "vue";
import App from "./App.vue";
import Home from "./pages/Home.vue";
import "@/styles/global.scss";

const routes = [
    { path: '/', redirect: '/home' },
    { path: '/home', name: 'Home', component: Home },
    { path: '/settings', name: 'Settings', component: Settings },
    { path: '/profile', name: 'Profile', component: Profile }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

createApp(App).use(router).mount('#app');
