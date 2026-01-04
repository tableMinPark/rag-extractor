import { client } from './client'
import { Passage } from '@/types/domain'
import { ApiResponse, PageResponse } from '@/types/api'

export interface GetPassageResponse extends Passage {}

export interface GetPassagesResponse extends PageResponse {
  content: Passage[]
}

/**
 * 패시지 목록 조회 API
 *
 * @param page 페이지
 * @param size 사이즈
 */
export const getPassagesApi = async (
  page: number,
  size: number,
  sourceId: number,
): Promise<ApiResponse<GetPassagesResponse>> => {
  const param = `page=${page}&size=${size}&sourceId=${sourceId}`
  const response = await client.get<ApiResponse<GetPassagesResponse>>(
    `/passage?${param}`,
  )

  return response.data
}

/**
 * 패시지 조회 API
 *
 * @param passageId 패시지 ID
 */
export const getPassageApi = async (
  passageId: number,
): Promise<ApiResponse<GetPassageResponse>> => {
  const response = await client.get<ApiResponse<GetPassageResponse>>(
    `/passage/${passageId}`,
  )

  return response.data
}
