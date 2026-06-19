import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAccessToken } from '@/utils/auth'

export function setupGuards(router: Router) {
  router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const token = getAccessToken()

    if (to.path === '/login') {
      if (token) {
        if (userStore.role === 'LIBRARIAN') {
          return next('/dashboard')
        } else if (userStore.role === 'READER') {
          return next('/reader/dashboard')
        }
      }
      return next()
    }

    if (to.path === '/') {
      if (token) {
        if (userStore.role === 'LIBRARIAN') {
          return next('/dashboard')
        } else if (userStore.role === 'READER') {
          return next('/reader/dashboard')
        }
      }
      return next('/login')
    }

    if (to.meta.requiresAuth) {
      if (!token) {
        return next('/login')
      }

      if (to.meta.roles && Array.isArray(to.meta.roles)) {
        if (!to.meta.roles.includes(userStore.role)) {
          return next('/403')
        }
      }
    }

    next()
  })
}
