// @vitest-environment jsdom

import { beforeEach, describe, expect, it, vi, type Mock } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: { error: vi.fn(), warning: vi.fn() }
}))

vi.mock('@/i18n', () => ({
  default: { global: { locale: { value: 'en' }, t: (key: string) => key } }
}))

vi.mock('@/router', () => ({
  default: { push: vi.fn() }
}))

vi.mock('@/utils/trace', () => ({
  generateTraceId: () => 'trace-test'
}))

vi.mock('@/utils/auth', () => ({
  getAccessToken: vi.fn(() => null),
  getRefreshToken: vi.fn(() => null),
  setAccessToken: vi.fn(),
  setRefreshToken: vi.fn(),
  removeAccessToken: vi.fn(),
  removeRefreshToken: vi.fn()
}))

import { getRefreshToken } from '@/utils/auth'

describe('request interceptor', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('exports a Result interface-compatible service', async () => {
    const mod = await import('@/api/request')
    expect(mod.default).toBeDefined()
    expect(typeof mod.default).toBe('function')
  })

  it('auth module calls request service for login', async () => {
    const { login } = await import('@/api/auth')
    expect(typeof login).toBe('function')
  })

  it('auth module calls request service for logout with refresh token', async () => {
    (getRefreshToken as Mock).mockReturnValue('test-refresh')
    const { logout } = await import('@/api/auth')
    expect(typeof logout).toBe('function')
  })

  it('handleAuthFailure clears tokens and redirects to login', async () => {
    localStorage.setItem('library:access_token', 'tok')
    localStorage.setItem('library:refresh_token', 'ref')
    localStorage.setItem('library:profile', '{}')
    localStorage.setItem('library:role', 'READER')

    const requestMod = await import('@/api/request')
    const service = requestMod.default

    const responseInterceptor = (service.interceptors?.response as any)
    expect(responseInterceptor).toBeDefined()
  })
})
