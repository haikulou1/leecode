import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/ProductListView.vue') },
  { path: '/cart', component: () => import('../views/CartView.vue') },
  { path: '/checkout', component: () => import('../views/CheckoutView.vue') },
  { path: '/success/:orderId', component: () => import('../views/OrderSuccessView.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
