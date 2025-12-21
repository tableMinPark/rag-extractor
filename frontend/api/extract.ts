import { ExtractContent } from '@/types/domain'
import { client } from './client'
import { ApiResponse } from '@/types/api'

export interface ExtractResponse {
  name: string
  ext: string
  lines: ExtractContent[]
}

/**
 * 파일 추출 API
 *
 * @param extractType 추출 타입
 * @param uploadFile 추출 파일
 */
export const extractFileApi = async (
  extractType: string,
  uploadFile: File,
): Promise<ApiResponse<ExtractResponse>> => {
  const formData = new FormData()
  formData.append('uploadFile', uploadFile)

  const response = await client.post<ApiResponse<ExtractResponse>>(
    `/extract/file/${extractType}`,
    formData,
  )

  return response.data
}

/**
 * 파일 텍스트 추출 API
 *
 * @param uploadFile 추출 파일
 */
export const extractFileTextApi = async (
  uploadFile: File,
): Promise<ApiResponse<string>> => {
  const formData = new FormData()
  formData.append('uploadFile', uploadFile)

  const response = await client.post<ApiResponse<string>>(
    `/extract/text`,
    formData,
  )

  return response.data
}
