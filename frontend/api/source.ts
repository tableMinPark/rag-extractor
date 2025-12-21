import { client } from './client'
import { Category, PatternType, RepoResource, Source } from '@/types/domain'
import { ApiResponse, PageResponse } from '@/types/api'

export interface GetSourceResponse extends Source {}

export interface GetSourcesResponse extends PageResponse {
  content: Source[]
}

export interface GetCategorySource extends Category {}

/**
 * 문서 목록 조회 API
 *
 * @param page 페이지
 * @param size 사이즈
 * @param keyword 검색어
 * @param categoryCode 카테고리코드
 * @param orderBy 정렬 필드
 * @param order 정렬 방향
 */
export const getSourcesApi = async (
  page: number,
  size: number,
  keyword?: string,
  categoryCode?: string,
  orderBy?: string,
  order?: 'asc' | 'desc',
): Promise<ApiResponse<GetSourcesResponse>> => {
  let param = `page=${page}&size=${size}`
  param += keyword ? `&keyword=${keyword}` : ''
  param += categoryCode ? `&categoryCode=${categoryCode}` : ''
  param += orderBy ? `&orderBy=${orderBy}` : ''
  param += order ? `&order=${order}` : ''

  const response = await client.get<ApiResponse<GetSourcesResponse>>(
    `/source?${param}`,
  )

  return response.data
}

/**
 * 문서 조회 API
 *
 * @param sourceId 문서 ID
 */
export const getSourceApi = async (
  sourceId: number,
): Promise<ApiResponse<GetSourceResponse>> => {
  const response = await client.get<ApiResponse<GetSourceResponse>>(
    `/source/${sourceId}`,
  )

  return response.data
}

/**
 * 문서 카테고리 목록 조회 API
 */
export const getCategoriesSourceApi = async (): Promise<
  ApiResponse<GetCategorySource[]>
> => {
  const response =
    await client.get<ApiResponse<GetCategorySource[]>>(`/source/category`)

  return response.data
}

/**
 * 파일 문서 등록 API
 *
 * @param categoryCode 카테고리 코드
 * @param collectionId 색인 테이블 ID
 * @param maxTokenSize 최대 토큰 수
 * @param overlapSize 오버랩 크기
 * @param patterns 청킹 패턴
 * @param stopPatterns 정지 패턴
 * @param selectType 전처리 타입
 * @param isAuto 자동화 데이터 여부
 */
export const createFileSourceApi = async (
  categoryCode: string,
  collectionId: string,
  maxTokenSize: number,
  overlapSize: number,
  patterns: PatternType[],
  stopPatterns: string[],
  selectType: string,
  isAuto: boolean,
  uploadFiles: File[],
): Promise<ApiResponse<void>> => {
  const formData = new FormData()
  uploadFiles.forEach((uploadFile) => formData.append('uploadFile', uploadFile))
  formData.append(
    'requestDto',
    JSON.stringify({
      categoryCode,
      collectionId,
      maxTokenSize,
      overlapSize,
      patterns,
      stopPatterns,
      selectType,
      isAuto,
    }),
  )

  const response = await client.post<ApiResponse<void>>(
    `/source/file`,
    formData,
  )

  return response.data
}

/**
 * 원격 문서 등록 API
 *
 * @param categoryCode 카테고리 코드
 * @param collectionId 색인 테이블 ID
 * @param maxTokenSize 최대 토큰 수
 * @param overlapSize 오버랩 크기
 * @param patterns 청킹 패턴
 * @param stopPatterns 정지 패턴
 * @param selectType 전처리 타입
 * @param isAuto 자동화 데이터 여부
 * @param host 원격지 호스트
 * @param port 원격지 포트
 * @param repoResources 원격 문서 정보
 */
export const createRepoSourcesApi = async (
  categoryCode: string,
  collectionId: string,
  maxTokenSize: number,
  overlapSize: number,
  patterns: PatternType[],
  stopPatterns: string[],
  selectType: string,
  isAuto: boolean,
  host: string,
  port: number,
  repoResources: RepoResource[],
): Promise<ApiResponse<void>> => {
  const response = await client.post<ApiResponse<void>>(`/source/repo`, {
    categoryCode,
    collectionId,
    maxTokenSize,
    overlapSize,
    patterns,
    stopPatterns,
    selectType,
    isAuto,
    host,
    port,
    repoResources,
  })

  return response.data
}
