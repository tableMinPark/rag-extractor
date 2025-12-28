'use client'

import { useState, useEffect, Suspense } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import { FolderOpen, Loader2, AlertCircle, Plus } from 'lucide-react'
import { Chunk, Passage } from '@/types/domain'
import { getPassageApi } from '@/api/passage'
import { getChunksApi } from '@/api/chunk'

function PassageDetailContent() {
  // ###################################################
  // 훅 및 파라미터 정의 (Hooks & Params)
  // ###################################################
  const ITEMS_PER_PAGE = 10
  const router = useRouter()
  const searchParams = useSearchParams()
  const passageId = Number(searchParams.get('passageId'))

  // ###################################################
  // 상태 정의 (State)
  // ###################################################
  const [passage, setPassage] = useState<Passage | null>(null)
  const [chunkList, setChunkList] = useState<Chunk[]>([])
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(ITEMS_PER_PAGE)
  const [totalPages, setTotalPages] = useState(0)
  const [totalCounts, setTotalCounts] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadData = async () => {
    if (!passageId || Number.isNaN(passageId)) {
      setError('패시지를 불러올 수 없습니다.')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setError(null)

    await getPassageApi(passageId)
      .then((response) => {
        console.log(`📡 ${response.message}`)
        setPassage(response.result)
      })
      .catch((error) => {
        console.error(error)
        setError('패시지를 불러올 수 없습니다.')
      })

    await getChunksApi(page, size, passageId)
      .then((response) => {
        console.log(`📡 ${response.message}`)
        setPage(response.result.pageNo)
        setSize(response.result.pageSize)
        setTotalPages(response.result.totalPages)
        setTotalCounts(response.result.totalCount)
        setChunkList(response.result.content)
      })
      .catch((error) => {
        console.error(error)
        setError('청크를 불러올 수 없습니다.')
      })

    setIsLoading(false)
  }

  // ###################################################
  // 이펙트 및 로직 (Effects & Logic)
  // ###################################################
  useEffect(() => {
    if (!passageId || Number.isNaN(passageId)) {
      setError('패시지를 불러올 수 없습니다.')
      setIsLoading(false)
      return
    }

    loadData()
  }, [page, size])

  useEffect(() => {
    if (!passageId || Number.isNaN(passageId)) {
      setError('패시지를 불러올 수 없습니다.')
      setIsLoading(false)
      return
    }

    loadData()
  }, [passageId])

  // ###################################################
  // 핸들러 (Handler)
  // ###################################################
  /**
   * 청크 등록 화면으로 이동 핸들러
   */
  const handleCreateChunk = () => {
    if (passage) {
      router.push(`/chunk/create?passageId=${passage.passageId}`)
    }
  }

  /**
   * TODO: 청크 무한 스크롤 연결 필요
   */
  const handleNextPage = () => {
    if (page < totalPages) {
      setPage((prev) => prev + 1)
    }
  }

  // ###################################################
  // 렌더링 (Render)
  // ###################################################
  // 1. 로딩 상태
  if (isLoading) {
    return (
      <div className="flex h-full w-full flex-col items-center justify-center gap-3">
        <Loader2 className="text-primary h-10 w-10 animate-spin" />
        <p className="text-sm font-medium text-gray-500">
          패시지 정보를 불러오는 중입니다...
        </p>
      </div>
    )
  }

  // 2. 에러 상태
  if (error || !passage) {
    return (
      <div className="flex h-full w-full flex-col items-center justify-center gap-3">
        <AlertCircle className="h-10 w-10 text-red-500" />
        <p className="text-sm font-bold text-gray-700">
          {error || '데이터가 존재하지 않습니다.'}
        </p>
        <button
          onClick={() => router.back()}
          className="text-primary mt-2 text-xs font-bold hover:underline"
        >
          ← 뒤로가기
        </button>
      </div>
    )
  }

  // 3. 정상 렌더링
  return (
    <div className="flex h-full w-full flex-col p-6">
      {/* 1. 상단 네비게이션 */}
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div>
            <h2 className="flex items-center gap-2 text-2xl font-bold text-gray-800">
              <FolderOpen className="text-primary h-6 w-6" />
              패시지 상세
            </h2>
            <p className="mt-1 text-xs text-gray-500">
              패시지 상세 정보 & 청크 목록
            </p>
          </div>
        </div>
        {/* 뒤로가기 버튼 */}
        <button
          onClick={() => router.back()}
          className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-bold text-gray-600 shadow-sm transition-colors hover:bg-gray-50 active:scale-95"
        >
          <span>← 뒤로가기</span>
        </button>
      </div>

      {/* 2. 메인 컨텐츠 영역 (좌우 분할) */}
      <div className="flex min-h-0 flex-1 gap-6">
        {/* [왼쪽] Passage 상세 내용 */}
        <div className="flex flex-[2] flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
          <div className="border-b border-gray-100 bg-gray-50 px-6 py-4">
            <h3 className="font-bold text-gray-800">패시지 원문 데이터</h3>
          </div>

          <div className="flex-1 overflow-auto p-8">
            <div className="mx-auto flex max-w-3xl flex-col gap-8">
              {/* (1) 제목 영역 */}
              <div className="flex flex-col gap-2 border-b border-gray-100 pb-6">
                {passage.title && (
                  <h1 className="text-2xl font-bold text-gray-900">
                    {passage.title}
                  </h1>
                )}
                {passage.subTitle && (
                  <h2 className="flex items-center gap-2 text-lg font-semibold text-gray-700">
                    <span className="bg-primary h-4 w-1 rounded-full"></span>
                    {passage.subTitle}
                  </h2>
                )}
                {passage.thirdTitle && (
                  <h3 className="text-md border-l-2 border-gray-200 pl-3 font-medium text-gray-600">
                    {passage.thirdTitle}
                  </h3>
                )}
                {!passage.title && !passage.subTitle && !passage.thirdTitle && (
                  <span className="text-gray-400 italic">(제목 없음)</span>
                )}
              </div>

              {/* (2) 본문 영역 */}
              <div>
                <label className="mb-2 block text-xs font-bold text-gray-400 uppercase">
                  본문 (Content)
                </label>
                <div className="text-base leading-8 whitespace-pre-wrap text-gray-800">
                  {passage.content}
                </div>
              </div>

              {/* (3) 부가 본문 영역 */}
              {passage.subContent && (
                <div className="rounded-lg border border-gray-100 bg-gray-50 p-5">
                  <label className="mb-2 block text-xs font-bold text-gray-400 uppercase">
                    부가 본문 (Sub Content)
                  </label>
                  <div className="text-sm leading-relaxed whitespace-pre-wrap text-gray-600">
                    {passage.subContent}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* [오른쪽] Chunk 목록 */}
        <div className="flex flex-1 flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
          {/* 헤더: 카운트 & 등록 버튼 */}
          <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-6 py-4">
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-gray-800">청크 목록</h3>
              <span className="rounded-full bg-blue-100 px-2 py-0.5 text-xs font-bold text-blue-700">
                {totalCounts}
              </span>
            </div>

            {/* 청크 등록 버튼 */}
            <button
              onClick={handleCreateChunk}
              className="hover:border-primary hover:text-primary flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-1.5 text-xs font-bold text-gray-600 shadow-sm transition-all active:scale-95"
            >
              <Plus className="h-3.5 w-3.5" />
              청크 등록
            </button>
          </div>

          <div className="flex-1 overflow-auto bg-gray-50/50 p-4">
            <div className="flex flex-col gap-3">
              {chunkList.map((chunk) => (
                <Link
                  key={chunk.chunkId}
                  href={`/chunk/detail?chunkId=${chunk.chunkId}`}
                  className="hover:border-primary group relative block overflow-hidden rounded-xl border border-gray-200 bg-white p-5 transition-all hover:shadow-md"
                >
                  {/* 카드 헤더 */}
                  <div className="mb-3 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-gray-500">
                        #{chunk.chunkId}
                      </span>
                      <span className="rounded border border-gray-200 bg-white px-2 py-0.5 text-[10px] font-bold text-gray-500">
                        v{chunk.version}
                      </span>
                    </div>
                    <span className="text-[10px] font-medium text-gray-400">
                      {chunk.compactContentTokenSize} Tokens
                    </span>
                  </div>

                  {/* 카드 본문 미리보기 */}
                  <p className="line-clamp-3 text-sm leading-relaxed text-gray-600 group-hover:text-gray-900">
                    {chunk.content}
                  </p>

                  {/* 호버 화살표 */}
                  <div className="text-primary absolute right-4 bottom-4 -translate-x-2 opacity-0 transition-all group-hover:translate-x-0 group-hover:opacity-100">
                    →
                  </div>
                </Link>
              ))}

              {chunkList.length === 0 && (
                <div className="flex h-40 flex-col items-center justify-center text-gray-400">
                  <span className="mb-2 text-2xl">📭</span>
                  <span className="text-sm">생성된 청크가 없습니다.</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function PassageDetailPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-screen items-center justify-center">
          <Loader2 className="h-10 w-10 animate-spin text-blue-500" />
        </div>
      }
    >
      <PassageDetailContent />
    </Suspense>
  )
}
