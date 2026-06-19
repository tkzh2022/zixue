// @vitest-environment jsdom

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}))

import { login, logout } from '@/api/auth'
import { useUserStore } from '@/stores/user'

describe('user store', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('persists tokens, profile and role after login', async () => {
    vi.mocked(login).mockResolvedValue({
      accessToken: 'access',
      refreshToken: 'refresh',
      user: { id: 1, username: 'librarian', role: 'LIBRARIAN' }
    } as never)
    const store = useUserStore()

    await store.login({ username: 'librarian', password: 'secret' })

    expect(localStorage.getItem('library:access_token')).toBe('access')
    expect(localStorage.getItem('library:refresh_token')).toBe('refresh')
    expect(localStorage.getItem('library:role')).toBe('LIBRARIAN')
    expect(store.role).toBe('LIBRARIAN')
  })

  it('clears local auth state even when server logout fails', async () => {
    localStorage.setItem('library:access_token', 'access')
    localStorage.setItem('library:refresh_token', 'refresh')
    localStorage.setItem('library:role', 'READER')
    vi.mocked(logout).mockRejectedValue(new Error('offline'))
    const store = useUserStore()

    await expect(store.logout()).rejects.toThrow('offline')

    expect(localStorage.getItem('library:access_token')).toBeNull()
    expect(localStorage.getItem('library:refresh_token')).toBeNull()
    expect(localStorage.getItem('library:role')).toBeNull()
  })
})
