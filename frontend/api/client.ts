import { config } from '@/public/ts/config'
import axios, {
  AxiosError,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios'

export const client = axios.create({
  // baseURL: '/api',
  baseURL: `http://${config.apiHost}:${config.apiPort}${config.apiBasePath}`,
  // headers: {
  //   'Content-Type': 'application/json',
  // },
  withCredentials: true,
})

// [요청 인터셉터]
client.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const accessToken =
      typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null

    if (accessToken && config.headers) {
      config.headers.Authorization = accessToken
    }

    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)

// [응답 인터셉터]
client.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  async (error: AxiosError) => {
    if (error.response) {
      if (error.response.status === 401) {
        // 로그인 화면이 아닌 경우
        if (
          typeof window !== 'undefined' &&
          window.location.pathname !== `${config.basePath}/login`
        ) {
          // 토큰 삭제
          localStorage.removeItem('accessToken')
          localStorage.removeItem('username')
          localStorage.removeItem('role')
          // 로그인 화면 이동
          window.location.href = `${config.basePath}/login`
        }
      } else if (error.response.status === 403) {
        if (typeof window !== 'undefined') {
          // 토큰 삭제
          localStorage.removeItem('accessToken')
          localStorage.removeItem('username')
          localStorage.removeItem('role')
          // 로그인 화면 이동
          window.location.href = `${config.basePath}/login`
        }
      }
    }
    return Promise.reject(error)
  },
)
