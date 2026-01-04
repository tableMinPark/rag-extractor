import { ApiResponse } from '@/types/api'
import { client } from './client'

export interface LoginResponse {
  username: string,
  role: string,
}

/**
 * 로그인 API
 *
 * @param username 유저명
 * @param password 패스워드
 */
export const loginApi = async (
  username: string,
  password: string,
): Promise<ApiResponse<LoginResponse>> => {
  const response = await client.post<ApiResponse<LoginResponse>>('/login', {
    username,
    password,
  })

  const accessToken = response.headers['authorization']

  if (accessToken) {
    localStorage.setItem('accessToken', accessToken)
  }

  return response.data
}
