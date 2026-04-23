'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { FolderOpen, Edit, Loader2, AlertCircle, Delete } from 'lucide-react'
import { Chunk } from '@/types/domain'
import { deleteChunkApi, getChunkApi } from '@/api/chunk'
import { getRole } from '@/public/ts/storageUtil'

function ChunkDetailContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const chunkId = Number(searchParams.get('chunkId'))

  const [chunk, setChunk] = useState<Chunk | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isDeleting, setIsDeleting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadData = async () => {
    if (!chunkId || Number.isNaN(chunkId)) {
      setError('청크를 불러올 수 없습니다.')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setError(null)

    try {
      const response = await getChunkApi(chunkId)
      setChunk(response.result)
    } catch (err) {
      console.error(err)
      setError('청크를 불러올 수 없습니다.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    if (!chunkId || Number.isNaN(chunkId)) {
      setError('청크를 불러올 수 없습니다.')
      setIsLoading(false)
      return
    }
    loadData()
  }, [chunkId])

  const handleEdit = () => {
    if (chunk) {
      router.push(`/chunk/edit?chunkId=${chunk.chunkId}`)
    }
  }

  const handleDelete = async () => {
    if (confirm('삭제된 청크는 복구할 수 없습니다. 삭제하시겠습니까?')) {
      setIsDeleting(true)
      await deleteChunkApi(chunkId)
        .then((response) => {
          console.log(response.message)
          router.back()
        })
        .catch((reason) => {
          console.error(reason)
        })
        .finally(() => {
          setIsDeleting(false)
        })
    }
  }

  if (isLoading) {
    return (
      <div className="flex h-full w-full flex-col items-center justify-center gap-3">
        <Loader2 className="text-primary h-10 w-10 animate-spin" />
        <p className="text-sm font-medium text-gray-500">데이터를 불러오는 중입니다...</p>
      </div>
    )
  }

  if (error || !chunk) {
    return (
      <div className="flex h-full w-full flex-col items-center justify-center gap-3">
        <AlertCircle className="h-10 w-10 text-red-500" />
        <p className="text-sm font-bold text-gray-700">{error || '데이터가 존재하지 않습니다.'}</p>
        <button onClick={() => router.back()} className="text-primary mt-2 text-xs font-bold hover:underline">
          ← 목록으로 돌아가기
        </button>
      </div>
    )
  }

  return (
    <div className="flex h-full w-full flex-col p-6">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="flex items-center gap-2 text-2xl font-bold text-gray-800">
            <FolderOpen className="text-primary h-6 w-6" />
            청크 상세
          </h2>
          <p className="mt-1 text-xs text-gray-500">청크 상세 정보</p>
        </div>

        <div className="flex items-center gap-3">
          {(getRole() === 'ROLE_ADMIN' || getRole() === 'ROLE_MANAGER') && (
            <button
              onClick={handleDelete}
              disabled={isDeleting}
              className="bg-primary hover:bg-primary-hover flex w-fit items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold text-white shadow-sm transition-all active:scale-95"
            >
              {isDeleting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Delete className="h-4 w-4" />}
              삭제
            </button>
          )}
          <button
            onClick={handleEdit}
            className="bg-primary hover:bg-primary-hover flex w-fit items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold text-white shadow-sm transition-all active:scale-95"
          >
            <Edit className="h-4 w-4" />
            수정
          </button>
          <button
            onClick={() => router.back()}
            className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-bold text-gray-600 shadow-sm transition-colors hover:bg-gray-50 active:scale-95"
          >
            ← 뒤로가기
          </button>
        </div>
      </div>

      <div className="flex min-h-0 flex-1 flex-col">
        <div className="flex w-full flex-1 flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-6 py-4">
            <div className="flex items-center gap-3">
              <h2 className="text-lg font-bold text-gray-800">청크 원문 데이터</h2>
              <span className="rounded border border-gray-200 bg-white px-2 py-0.5 text-[10px] font-bold text-gray-500">v{chunk.version}</span>
            </div>
            <span className="font-mono text-xs text-gray-400">최종수정: {chunk.sysModifyDt}</span>
          </div>

          <div className="flex flex-col gap-8 overflow-y-auto p-8">
            <div className="rounded-xl border border-gray-100 bg-gray-50 p-6">
              <label className="mb-4 block text-xs font-bold text-gray-400 uppercase">청크 메타 데이터 (Chunk Metadata)</label>
              <div className="grid grid-cols-2 gap-6 sm:grid-cols-5">
                <div className="flex flex-col gap-1">
                  <span className="text-xs text-gray-500">청크 ID</span>
                  <span className="font-bold text-gray-800">{chunk.chunkId}</span>
                </div>
                <div className="flex flex-col gap-1">
                  <span className="text-xs text-gray-500">본문 토큰 수</span>
                  <span className="font-bold text-gray-800">{chunk.contentTokenSize}</span>
                </div>
                <div className="flex flex-col gap-1">
                  <span className="text-xs text-gray-500">색인 본문 토큰 수</span>
                  <span className="text-primary font-bold">{chunk.compactContentTokenSize}</span>
                </div>
                <div className="flex flex-col gap-1">
                  <span className="text-xs text-gray-500">등록일</span>
                  <span className="text-xs font-bold text-gray-800">{chunk.sysCreateDt}</span>
                </div>
                <div className="flex flex-col gap-1">
                  <span className="text-xs text-gray-500">수정일</span>
                  <span className="text-xs font-bold text-gray-800">{chunk.sysModifyDt}</span>
                </div>
              </div>
            </div>

            <div className="flex flex-col gap-2 border-b border-gray-100 pb-6">
              {chunk.title && <h1 className="text-xl font-bold text-gray-900">{chunk.title}</h1>}
              {chunk.subTitle && (
                <h2 className="text-md flex items-center gap-2 font-semibold text-gray-700">
                  <span className="bg-primary h-3 w-1 rounded-full"></span>
                  {chunk.subTitle}
                </h2>
              )}
              {chunk.thirdTitle && (
                <h3 className="border-l-2 border-gray-200 pl-3 text-sm font-medium text-gray-600">{chunk.thirdTitle}</h3>
              )}
              {!chunk.title && !chunk.subTitle && !chunk.thirdTitle && (
                <span className="text-gray-400 italic">(제목 없음)</span>
              )}
            </div>

            {chunk.compactContent && (
              <div>
                <label className="mb-2 block text-xs font-bold text-blue-500 uppercase">색인 본문 (Compact Content)</label>
                <div className="rounded-lg border border-blue-100 bg-blue-50 p-5 text-sm leading-relaxed whitespace-pre-wrap text-gray-700">{chunk.compactContent}</div>
              </div>
            )}

            <div>
              <label className="mb-2 block text-xs font-bold text-gray-400 uppercase">본문 (Content)</label>
              <div className="rounded-lg border border-gray-100 bg-gray-50 p-6 text-base leading-8 whitespace-pre-wrap text-gray-800">{chunk.content}</div>
            </div>

            {chunk.subContent && (
              <div>
                <label className="mb-2 block text-xs font-bold text-gray-400 uppercase">부가 본문 (Sub Content)</label>
                <div className="border-l-2 border-gray-200 pl-2 text-sm leading-relaxed whitespace-pre-wrap text-gray-500">{chunk.subContent}</div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default function ChunkDetailPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-screen items-center justify-center">
          <Loader2 className="h-10 w-10 animate-spin text-blue-500" />
        </div>
      }
    >
      <ChunkDetailContent />
    </Suspense>
  )
}
