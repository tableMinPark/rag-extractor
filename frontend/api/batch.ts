import { client } from './client'
import { ApiResponse } from '@/types/api'

export interface ChunkBatchResponse {
  isConvertError: boolean
  fileName: string
  version: number
  totalPassageCount: number
  totalChunkCount: number
}

/**
 * 대상 문서 청킹 배치 API
 *
 * @param sourceId 대상 문서 ID
 */
export const chunkBatchApi = async (
  sourceId: number,
): Promise<ApiResponse<ChunkBatchResponse>> => {
  const response = await client.post<ApiResponse<ChunkBatchResponse>>(
    `/batch/chunk/${sourceId}`,
  )

  return response.data
}
