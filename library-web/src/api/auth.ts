import request from './request'
import { getRefreshToken } from '@/utils/auth'

export function login(data: any) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data: any) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post',
    data: { refreshToken: getRefreshToken() }
  })
}
