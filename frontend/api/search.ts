import { client } from './client'
import { ApiResponse } from '@/types/api'

// ─── 타입 정의 ────────────────────────────────────────────

export interface SearchCollection {
  collectionId: string
  collectionName: string
}

export interface SearchCategory {
  code: string
  name: string
}

export interface SearchRequestBody {
  query: string
  collectionId: string
  topK: number
  aliases: string[]
}

export interface SearchResultItem {
  chunkId: number
  passageId: number
  sourceId: number
  fileDetailId: number
  originFileName: string
  name: string
  title: string
  subTitle: string
  thirdTitle: string
  compactContent: string
  content: string
  subContent: string
  context: string
  url: string
  categoryCode: string
  sourceType: string
  ext: string
  alias: string
  sysCreateDt: string
  sysModifyDt: string
  score: number
}

// ─── API 함수 ────────────────────────────────────────────

export const getCollectionsApi = async (): Promise<ApiResponse<SearchCollection[]>> => {
  const response = await client.get<ApiResponse<SearchCollection[]>>('/collection')
  return response.data
}

export const getSearchCategoriesApi = async (): Promise<ApiResponse<SearchCategory[]>> => {
  const response = await client.get<ApiResponse<SearchCategory[]>>('/search/category')
  return response.data
}

export const keywordSearchApi = async (
  body: SearchRequestBody,
): Promise<ApiResponse<SearchResultItem[]>> => {
  const response = await client.post<ApiResponse<SearchResultItem[]>>('/search/keyword', body)
  return response.data
}

export const vectorSearchApi = async (
  body: SearchRequestBody,
): Promise<ApiResponse<SearchResultItem[]>> => {
  const response = await client.post<ApiResponse<SearchResultItem[]>>('/search/vector', body)
  return response.data
}
