import { client } from './client'
import { ApiResponse } from '@/types/api'

export interface EmbedResponse {
  successCount: number
  failCount: number
}

export const embedSourceApi = async (
  sourceId: number,
): Promise<ApiResponse<EmbedResponse>> => {
  const response = await client.post<ApiResponse<EmbedResponse>>(
    `/embed/${sourceId}`,
  )
  return response.data
}

export const deleteEmbedApi = async (
  sourceId: number,
): Promise<ApiResponse<void>> => {
  const response = await client.delete<ApiResponse<void>>(`/embed/${sourceId}`)
  return response.data
}
