import { client } from './client'
import { Chunk } from '@/types/domain'
import { ApiResponse, PageResponse } from '@/types/api'

export interface GetChunkResponse extends Chunk {}

export interface GetChunksResponse extends PageResponse {
  content: Chunk[]
}

/**
 * 청크 목록 조회 API
 *
 * @param page 페이지
 * @param size 사이즈
 * @param passageId 패시지 ID
 */
export const getChunksApi = async (
  page: number,
  size: number,
  passageId: number,
): Promise<ApiResponse<GetChunksResponse>> => {
  const param = `page=${page}&size=${size}&passageId=${passageId}`
  const response = await client.get<ApiResponse<GetChunksResponse>>(
    `/chunk?${param}`,
  )

  return response.data
}

/**
 * 청크 조회 API
 *
 * @param chunkId 청크 ID
 */
export const getChunkApi = async (
  chunkId: number,
): Promise<ApiResponse<GetChunkResponse>> => {
  const response = await client.get<ApiResponse<GetChunkResponse>>(
    `/chunk/${chunkId}`,
  )

  return response.data
}

/**
 * 청크 등록 API
 *
 * @param passageId 패시지 ID
 * @param title 제목
 * @param subTitle 중제목
 * @param thirdTitle 소제목
 * @param content 본문
 * @param subContent 부가 본문
 */
export const createChunkApi = async (
  passageId: number,
  title: string,
  subTitle: string,
  thirdTitle: string,
  content: string,
  subContent: string,
): Promise<ApiResponse<void>> => {
  const response = await client.post<ApiResponse<void>>(`/chunk`, {
    passageId,
    title,
    subTitle,
    thirdTitle,
    content,
    subContent,
  })
  return response.data
}

/**
 * 청크 수정 API
 *
 * @param chunkId 청크 ID
 * @param title 제목
 * @param subTitle 중제목
 * @param thirdTitle 소제목
 * @param content 본문
 * @param subContent 부가 본문
 */
export const updateChunkApi = async (
  chunkId: number,
  title: string,
  subTitle: string,
  thirdTitle: string,
  content: string,
  subContent: string,
): Promise<ApiResponse<void>> => {
  const response = await client.put<ApiResponse<void>>(`/chunk/${chunkId}`, {
    title,
    subTitle,
    thirdTitle,
    content,
    subContent,
  })
  return response.data
}

/**
 * 청크 삭제
 *
 * @param chunkId 청크 ID
 */
export const deleteChunkApi = async (
  chunkId: number,
): Promise<ApiResponse<void>> => {
  const response = await client.delete<ApiResponse<void>>(`/chunk/${chunkId}`)
  return response.data
}
