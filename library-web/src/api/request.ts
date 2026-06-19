import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import i18n from '@/i18n'
import { getAccessToken, getRefreshToken, setAccessToken, setRefreshToken, removeAccessToken, removeRefreshToken } from '@/utils/auth'
import { generateTraceId } from '@/utils/trace'
import router from '@/router'

export interface Result<T = any> {
  code: number
  message: string
  data: T
  traceId: string
}

const service: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
})

let isRefreshing = false
let requestsQueue: Array<{
  resolve: (token: string) => void
  reject: (error: unknown) => void
}> = []

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    config.headers['X-Trace-Id'] = generateTraceId()
    config.headers['Accept-Language'] = i18n.global.locale.value
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data
    if (res.code === 0) {
      return res.data
    }

    return handleBusinessError(res, response.config)
  },
  (error) => {
    const result = error.response?.data as Result | undefined
    if (result && typeof result.code === 'number') {
      return handleBusinessError(result, error.config)
    }
    const { t } = i18n.global
    ElMessage.error(t('error.network'))
    return Promise.reject(error)
  }
)

function handleBusinessError(res: Result, config: AxiosRequestConfig) {
  const { t } = i18n.global

  if (res.code === 8402) {
    if (!isRefreshing) {
      isRefreshing = true
      const refreshToken = getRefreshToken()
      if (!refreshToken) {
        handleAuthFailure()
        isRefreshing = false
        return Promise.reject(new Error('No refresh token'))
      }

      return axios.post('/api/v1/auth/refresh', { refreshToken }).then((refreshRes) => {
        const refreshed = refreshRes.data as Result<any>
        if (refreshed.code !== 0) {
          throw new Error(refreshed.message)
        }
        const newAccess = refreshed.data.accessToken
        const newRefresh = refreshed.data.refreshToken
        setAccessToken(newAccess)
        setRefreshToken(newRefresh)
          
        requestsQueue.forEach(item => item.resolve(newAccess))
        requestsQueue = []
          
        config.headers = config.headers || {}
        config.headers['Authorization'] = `Bearer ${newAccess}`
        return service(config)
      }).catch((err) => {
        requestsQueue.forEach(item => item.reject(err))
        requestsQueue = []
        handleAuthFailure()
        return Promise.reject(err)
      }).finally(() => {
        isRefreshing = false
      })
    } else {
      return new Promise((resolve, reject) => {
        requestsQueue.push({
          resolve: (token: string) => {
            config.headers = config.headers || {}
            config.headers['Authorization'] = `Bearer ${token}`
            resolve(service(config))
          },
          reject
        })
      })
    }
  }

  if (res.code === 8401 || res.code === 8403 || res.code === 8405) {
    handleAuthFailure()
    ElMessage.warning(t(`error.${res.code}`) || res.message)
    return Promise.reject(new Error(res.message))
  }

  if (res.code === 8404) {
    ElMessage.error(t('error.8404') || res.message)
    return Promise.reject(new Error(res.message))
  }

  if (res.code === 8003) {
    ElMessage.warning(t('error.8003') || res.message)
    return Promise.reject(new Error(res.message))
  }

  ElMessage.warning(t(`error.${res.code}`) || res.message)
  return Promise.reject(new Error(res.message))
}

function handleAuthFailure() {
  removeAccessToken()
  removeRefreshToken()
  localStorage.removeItem('library:profile')
  localStorage.removeItem('library:role')
  router.push('/login')
}

export default service
