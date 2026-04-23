import { config } from '@/public/ts/config'
import { useModalStore } from '@/stores/modalStore'
import { useAuthStore } from '@/stores/authStore'
import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

const BASE_URL = `http://${config.apiHost}:${config.apiPort}${config.apiBasePath}`
const AUTH_PATHS = ['/auth/login', '/auth/register', '/auth/reissue']

export const client = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
})

let isRefreshing = false
let pendingQueue: Array<{
  resolve: (token: string) => void
  reject: (err: unknown) => void
}> = []

const processPendingQueue = (token: string | null, error: unknown = null) => {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (token) resolve(token)
    else reject(error)
  })
  pendingQueue = []
}

const redirectToLogin = () => {
  if (typeof window !== 'undefined') {
    window.location.href = `${config.basePath}/login`
  }
}

const redirectToHome = () => {
  if (typeof window !== 'undefined') {
    window.location.href = `${config.basePath}/`
  }
}

const isAuthRequest = (url?: string) => {
  if (!url) {
    return false
  }

  return AUTH_PATHS.some((path) => url.endsWith(path))
}

const reissueToken = async (): Promise<string> => {
  if (isRefreshing) {
    return new Promise<string>((resolve, reject) => {
      pendingQueue.push({ resolve, reject })
    })
  }

  isRefreshing = true
  try {
    const response = await axios.post<{ accessToken: string }>(
      `${BASE_URL}/auth/reissue`,
      {},
      { withCredentials: true },
    )
    const newToken = response.data.accessToken
    const { userId, name, role } = useAuthStore.getState()
    useAuthStore.getState().setAuth(newToken, userId ?? '', name ?? '', role ?? '')
    processPendingQueue(newToken)
    return newToken
  } catch (err) {
    processPendingQueue(null, err)
    useAuthStore.getState().clearAuth()
    redirectToLogin()
    throw err
  } finally {
    isRefreshing = false
  }
}

client.interceptors.request.use(
  (cfg: InternalAxiosRequestConfig) => {
    const token = typeof window !== 'undefined'
      ? useAuthStore.getState().accessToken
      : null

    if (token && cfg.headers) {
      cfg.headers.Authorization = `Bearer ${token}`
    }

    return cfg
  },
  (error: AxiosError) => Promise.reject(error),
)

client.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError & { config?: InternalAxiosRequestConfig & { _retry?: boolean } }) => {
    const originalRequest = error.config

    if (error.response?.status === 403) {
      useModalStore.getState().setError(
        '접근 권한 없음',
        '요청한 기능을 사용할 권한이 없습니다.',
        '권한이 있는 계정으로 다시 로그인하거나 관리자에게 문의해 주세요.',
        async () => {
          redirectToHome()
        },
      )
      return Promise.reject(error)
    }

    if (error.response?.status !== 401 || !originalRequest) {
      return Promise.reject(error)
    }

    if (isAuthRequest(originalRequest.url)) {
      return Promise.reject(error)
    }

    if (originalRequest._retry) {
      useAuthStore.getState().clearAuth()
      redirectToLogin()
      return Promise.reject(error)
    }

    originalRequest._retry = true

    try {
      const newToken = await reissueToken()
      originalRequest.headers = originalRequest.headers ?? {}
      originalRequest.headers.Authorization = `Bearer ${newToken}`
      return client(originalRequest)
    } catch (reissueError) {
      return Promise.reject(reissueError)
    }
  },
)
