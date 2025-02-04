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

// track the last visited route and redirect to the appropriate route when clicking Settings
router.beforeEach((to, _, next) => {
    // store the last visited route
    if (to.name === 'Home') {
        localStorage.setItem('lastRoute', 'home');
    } else if (to.name === 'Profile') {
        localStorage.setItem('lastRoute', 'profile');
    }

    // redirect to the appropriate route when clicking Settings
    if (to.name === 'Settings') {
        const lastRoute = localStorage.getItem('lastRoute');
        if (lastRoute === 'home') {
            next({ name: 'Home' });
        } else if (lastRoute === 'profile') {
            next({ name: 'Profile' });
        } else {
            next();
        }
    } else {
        next();
    }
});

createApp(App).use(router).mount('#app');
