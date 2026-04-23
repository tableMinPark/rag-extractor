import { client } from './client'
import { ApiResponse } from '@/types/api'

export interface ChunkBatchResponse {
  isConvertError: boolean
  previousPassages: number
  currentPassages: number
  chunks: number
}

/**
 * 대상 문서 청킹 배치 API
 *
 * @param sourceId 대상 문서 ID
 */
export const chunkBatchApi = async (
  sourceId: number,
): Promise<ApiResponse<ChunkBatchResponse[]>> => {
  const response = await client.post<ApiResponse<ChunkBatchResponse[]>>(
    `/chunk/source`,
    { sourceIds: [sourceId] },
  )

  return response.data
}

/**
 * 전체 배치 대상 문서 패시지 분리 배치 API
 */
export const batchPassagingApi = async (): Promise<
  ApiResponse<ChunkBatchResponse[]>
> => {
  const response = await client.post<ApiResponse<ChunkBatchResponse[]>>(
    `/chunk/source`,
    { sourceIds: [] },
  )

  return response.data
}
