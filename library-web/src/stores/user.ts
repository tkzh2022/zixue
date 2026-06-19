import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, register as registerApi } from '@/api/auth'
import { setAccessToken, setRefreshToken, removeAccessToken, removeRefreshToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const profile = ref<any>(JSON.parse(localStorage.getItem('library:profile') || 'null'))
  const role = ref<string>(localStorage.getItem('library:role') || '')

  async function login(loginForm: any) {
    const data = await loginApi(loginForm)
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    profile.value = data.user
    role.value = data.user.role
    localStorage.setItem('library:profile', JSON.stringify(data.user))
    localStorage.setItem('library:role', data.user.role)
    return data
  }

  async function register(registerForm: any) {
    return await registerApi(registerForm)
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      reset()
    }
  }

  function reset() {
    removeAccessToken()
    removeRefreshToken()
    profile.value = null
    role.value = ''
    localStorage.removeItem('library:profile')
    localStorage.removeItem('library:role')
  }

  return {
    profile,
    role,
    login,
    register,
    logout,
    reset
  }
})
