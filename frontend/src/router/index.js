import {createRouter, createWebHistory} from "vue-router";

import WelcomeView from "@/views/WelcomeView.vue";
import LoginPage from "@/views/welcome/LoginPage.vue";
import WelcomeFooter from "@/views/welcome/WelcomeFooter.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: WelcomeView,
            children:[
                {
                    path: '',
                    name: 'welcome-login',
                    component: LoginPage
                }
            ]
        },
        {

        }
    ]
})
export default router