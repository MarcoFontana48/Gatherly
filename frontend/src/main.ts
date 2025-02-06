import { createRouter, createWebHistory } from 'vue-router';
import Settings from './pages/Settings.vue';
import Profile from './pages/Profile.vue';
import { createApp } from "vue";
import App from "./App.vue";
import Home from "./pages/Home.vue";
import "@/styles/global.scss";
import Login from "./pages/Login.vue";
import {createPinia} from "pinia";

// Define routes
const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Login', component: Login },
    { path: '/home', name: 'Home', component: Home },
    { path: '/settings', name: 'Settings', component: Settings },
    { path: '/profile', name: 'Profile', component: Profile }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

function isAuthenticated() {
    return sessionStorage.getItem('authToken') !== null;
}

router.beforeEach((to, _, next) => {
    const routeName = to.name as string | undefined;

    if (shouldRedirectToLogin(routeName)) {
        next({ name: 'Login' });
    } else {
        handleAuthenticatedNavigation(routeName, next);
    }
});

function shouldRedirectToLogin(routeName: string | undefined): boolean {
    if (routeName && ['Home', 'Settings', 'Profile'].includes(routeName) && !isAuthenticated()) {
        console.log("cannot access this route without authentication, redirecting to login page");
        return true;
    }
    return false;
}

function handleAuthenticatedNavigation(routeName: string | undefined, next: Function) {
    console.log("Current auth token:" + sessionStorage.getItem('authToken'));

    if (routeName === 'Settings') {
        handleSettingsRedirection(next);
    } else {
        trackLastVisitedRoute(routeName);
        next();
    }
}

function trackLastVisitedRoute(routeName: string | undefined) {
    if (routeName === 'Home' || routeName === 'Profile') {
        sessionStorage.setItem('lastRoute', routeName.toLowerCase());
    }
}

function handleSettingsRedirection(next: Function) {
    const lastRoute = sessionStorage.getItem('lastRoute');
    if (lastRoute === 'home') {
        next({ name: 'Home' });
    } else if (lastRoute === 'profile') {
        next({ name: 'Profile' });
    } else {
        next();
    }
}

const app = createApp(App)
app.use(router)
app.use(createPinia());
app.use(router);
app.mount("#app");