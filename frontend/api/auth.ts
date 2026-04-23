import { ApiResponse } from '@/types/api'
import { client } from './client'

export interface LoginResponse {
  username: string
  role: string
  accessToken: string
}

export const loginApi = async (
  username: string,
  password: string,
): Promise<ApiResponse<LoginResponse>> => {
  const response = await client.post<ApiResponse<LoginResponse>>('/login', {
    username,
    password,
  })
  return response.data
}
