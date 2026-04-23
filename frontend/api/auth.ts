import { ApiResponse } from '@/types/api'
import { AxiosError } from 'axios'
import { client } from './client'

export interface LoginRequest {
  userId: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  userId: string
  name: string
  role: string
}

export interface RegisterRequest {
  userId: string
  password: string
  name: string
  email: string
}

interface ErrorMessageResponse {
  message?: string
}

export const loginApi = async (request: LoginRequest): Promise<LoginResponse> => {
  const response = await client.post<LoginResponse>('/auth/login', request)
  return response.data
}

export const registerApi = async (request: RegisterRequest): Promise<void> => {
  await client.post('/auth/register', request)
}

export const logoutApi = async (): Promise<void> => {
  await client.post('/auth/logout')
}

export const getAuthErrorMessage = (
  error: unknown,
  fallbackMessage: string,
): string => {
  const axiosError = error as AxiosError<
    ApiResponse<ErrorMessageResponse> | ErrorMessageResponse
  >
  const data = axiosError.response?.data

  if (!data) {
    return fallbackMessage
  }

  if ('result' in data && data.result?.message) {
    return data.result.message
  }

  if ('message' in data && typeof data.message === 'string') {
    return data.message
  }

  return fallbackMessage
}
