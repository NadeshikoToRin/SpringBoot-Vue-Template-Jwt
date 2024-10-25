import {createRouter, createWebHistory} from "vue-router";

import WelcomeView from "@/views/WelcomeView.vue";
import LoginPage from "@/views/welcome/LoginPage.vue";
import IndexView from "@/views/IndexView.vue";
import {unauthorized} from "@/net/index.js";
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
            path: '/index',
            name: 'index',
            component: IndexView
        }
    ]
})

router.beforeEach((to,from,next) =>{
    const isUnauthorized = unauthorized()
    if (to.name.startsWith('welcome-') && !isUnauthorized) {
        next('/index')
    }else if (to.fullPath.startsWith('/index') && isUnauthorized){
        next('/')
    }else {
        next()
    }
})

export default router