import { createRouter, createWebHistory } from 'vue-router'
import BlankLayout from '@/layouts/BlankLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import ReaderLayout from '@/layouts/ReaderLayout.vue'
import { setupGuards } from './guards'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: BlankLayout,
    },
    {
      path: '/auth',
      component: BlankLayout,
      children: [
        {
          path: '/login',
          name: 'Login',
          component: () => import('@/views/auth/Login.vue')
        },
        {
          path: '/register',
          name: 'Register',
          component: () => import('@/views/auth/Register.vue')
        }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, roles: ['LIBRARIAN'] },
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/Dashboard.vue')
        },
        {
          path: 'books',
          name: 'AdminBooks',
          component: () => import('@/views/books/BookList.vue'),
          meta: { title: 'Book Management', roles: ['LIBRARIAN'] }
        },
        {
          path: 'readers',
          name: 'AdminReaders',
          component: () => import('@/views/readers/ReaderList.vue'),
          meta: { title: 'Reader Management', roles: ['LIBRARIAN'] }
        },
        {
          path: 'rules',
          name: 'AdminRules',
          component: () => import('@/views/settings/RuleConfig.vue'),
          meta: { title: 'Borrow Rules', roles: ['LIBRARIAN'] }
        },
        {
          path: 'borrows',
          name: 'AdminBorrows',
          component: () => import('@/views/borrows/BorrowManage.vue'),
          meta: { title: 'Borrow Management', roles: ['LIBRARIAN'] }
        },
        {
          path: 'fines',
          name: 'AdminFines',
          component: () => import('@/views/fines/FineManage.vue'),
          meta: { title: 'Fine Management', roles: ['LIBRARIAN'] }
        }
      ]
    },
    {
      path: '/reader',
      component: ReaderLayout,
      meta: { requiresAuth: true, roles: ['READER'] },
      children: [
        {
          path: 'dashboard',
          name: 'ReaderDashboard',
          component: () => import('@/views/dashboard/Dashboard.vue'),
          meta: { title: 'Dashboard', roles: ['READER'] }
        },
        {
          path: '/catalog',
          name: 'Catalog',
          component: () => import('@/views/catalog/CatalogSearch.vue')
        },
        {
          path: '/catalog/books/:id',
          name: 'CatalogBookDetail',
          component: () => import('@/views/catalog/CatalogBookDetail.vue')
        },
        {
          path: 'borrows',
          name: 'MyBorrows',
          component: () => import('@/views/my/MyBorrows.vue'),
          meta: { title: 'My Borrows', roles: ['READER'] }
        },
        {
          path: 'fines',
          name: 'MyFines',
          component: () => import('@/views/my/MyFines.vue'),
          meta: { title: 'My Fines', roles: ['READER'] }
        }
      ]
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/Forbidden.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/NotFound.vue')
    }
  ]
})

setupGuards(router)

export default router
