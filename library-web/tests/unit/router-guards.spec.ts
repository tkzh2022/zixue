// @vitest-environment jsdom

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn()
}))

vi.mock('@/utils/auth', () => ({
  getAccessToken: vi.fn(() => null),
  getRefreshToken: vi.fn(() => null),
  setAccessToken: vi.fn(),
  setRefreshToken: vi.fn(),
  removeAccessToken: vi.fn(),
  removeRefreshToken: vi.fn()
}))

import { getAccessToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { setupGuards } from '@/router/guards'
import type { RouteLocationNormalized, NavigationGuardNext, Router } from 'vue-router'

type MockedFn = ReturnType<typeof vi.fn>

function createMockRoute(overrides: Partial<RouteLocationNormalized> = {}): RouteLocationNormalized {
  return {
    path: '/',
    name: undefined,
    params: {},
    query: {},
    hash: '',
    fullPath: '/',
    matched: [],
    redirectedFrom: undefined,
    meta: {},
    ...overrides
  } as RouteLocationNormalized
}

describe('router guards', () => {
  let guardFn: (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => void
  let next: MockedFn

  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    setActivePinia(createPinia())

    const mockRouter = {
      beforeEach: vi.fn((fn: any) => {
        guardFn = fn
      })
    } as unknown as Router

    setupGuards(mockRouter)
    next = vi.fn()
  })

  it('redirects unauthenticated users from requiresAuth routes to /login', () => {
    (getAccessToken as MockedFn).mockReturnValue(null)
    const to = createMockRoute({ path: '/dashboard', meta: { requiresAuth: true, roles: ['LIBRARIAN'] } })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith('/login')
  })

  it('redirects wrong role to /403', () => {
    (getAccessToken as MockedFn).mockReturnValue('valid-token')
    const store = useUserStore()
    store.role = 'READER'

    const to = createMockRoute({
      path: '/dashboard',
      meta: { requiresAuth: true, roles: ['LIBRARIAN'] }
    })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith('/403')
  })

  it('allows correct role through', () => {
    (getAccessToken as MockedFn).mockReturnValue('valid-token')
    const store = useUserStore()
    store.role = 'LIBRARIAN'

    const to = createMockRoute({
      path: '/dashboard',
      meta: { requiresAuth: true, roles: ['LIBRARIAN'] }
    })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })

  it('redirects authenticated LIBRARIAN from /login to /dashboard', () => {
    (getAccessToken as MockedFn).mockReturnValue('valid-token')
    const store = useUserStore()
    store.role = 'LIBRARIAN'

    const to = createMockRoute({ path: '/login' })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith('/dashboard')
  })

  it('redirects authenticated READER from /login to /catalog', () => {
    (getAccessToken as MockedFn).mockReturnValue('valid-token')
    const store = useUserStore()
    store.role = 'READER'

    const to = createMockRoute({ path: '/login' })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith('/catalog')
  })

  it('allows unauthenticated user to access /login', () => {
    (getAccessToken as MockedFn).mockReturnValue(null)
    const to = createMockRoute({ path: '/login' })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })

  it('redirects unauthenticated user from / to /login', () => {
    (getAccessToken as MockedFn).mockReturnValue(null)
    const to = createMockRoute({ path: '/' })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith('/login')
  })

  it('redirects authenticated LIBRARIAN from / to /dashboard', () => {
    (getAccessToken as MockedFn).mockReturnValue('valid-token')
    const store = useUserStore()
    store.role = 'LIBRARIAN'

    const to = createMockRoute({ path: '/' })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith('/dashboard')
  })

  it('allows through routes without requiresAuth', () => {
    (getAccessToken as MockedFn).mockReturnValue(null)
    const to = createMockRoute({ path: '/register', meta: {} })
    const from = createMockRoute()

    guardFn(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })
})
