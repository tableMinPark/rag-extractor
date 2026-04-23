'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { FolderOpen, Loader2, AlertCircle, RefreshCw, Database } from 'lucide-react'
import { getSourceApi } from '@/api/source'
import { getPassagesApi } from '@/api/passage'
import { Passage, Source } from '@/types/domain'
import { getRole } from '@/public/ts/storageUtil'
import { chunkBatchApi } from '@/api/batch'
import { embedSourceApi, deleteEmbedApi } from '@/api/embed'

const BatchLoadingModal = ({ message }: { message: string }) => (
  <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
    <div className="flex flex-col items-center justify-center gap-5 rounded-2xl bg-white p-10 shadow-2xl">
      <div className="relative">
        <div className="absolute inset-0 animate-ping rounded-full bg-blue-100 opacity-75"></div>
        <Loader2 className="relative h-12 w-12 animate-spin text-blue-600" />
      </div>
      <div className="text-center">
        <h3 className="text-lg font-bold text-gray-900">{message}</h3>
        <p className="mt-2 text-sm text-gray-500">
          잠시만 기다려주세요. (화면을 닫지 마세요)
        </p>
      </div>
    </div>
  </div>
)

function SourceDetailContent() {
  const ITEMS_PER_PAGE = 10
  const router = useRouter()
  const searchParams = useSearchParams()
  const sourceId = Number(searchParams.get('sourceId'))

  const [source, setSource] = useState<Source | null>(null)
  const [passageList, setPassageList] = useState<Passage[]>([])
  const [page, setPage] = useState(1)
  const [size] = useState(ITEMS_PER_PAGE)
  const [totalPages, setTotalPages] = useState(0)
  const [totalCounts, setTotalCounts] = useState(0)
  const [isBatch, setIsBatch] = useState(false)
  const [isEmbedding, setIsEmbedding] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadData = async () => {
    if (!sourceId || Number.isNaN(sourceId)) {
      setError('올바르지 않은 접근입니다.')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setError(null)

    try {
      const [sourceRes, passageRes] = await Promise.all([
        getSourceApi(sourceId),
        getPassagesApi(page, size, sourceId),
      ])
      setSource({ ...sourceRes.result, selectCode: sourceRes.result.selectType })
      setPage(passageRes.result.pageNo)
      setTotalPages(passageRes.result.totalPages)
      setTotalCounts(passageRes.result.totalCount)
      setPassageList(passageRes.result.content)
    } catch (err) {
      console.error(err)
      setError('데이터를 불러오는 중 오류가 발생했습니다.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    if (!sourceId || Number.isNaN(sourceId)) return
    loadData()
  }, [page, sourceId])

  const handleChunkBatch = async () => {
    if (!confirm('청킹 배치를 시작하시겠습니까?\n문서 크기에 따라 시간이 소요될 수 있습니다.')) return
    setIsBatch(true)
    try {
      await chunkBatchApi(sourceId)
      await loadData()
    } catch (error) {
      console.error(error)
      alert('작업 중 오류가 발생했습니다.')
    } finally {
      setIsBatch(false)
    }
  }

  const handleEmbed = async () => {
    if (!confirm('색인 처리를 시작하시겠습니까?')) return
    setIsEmbedding(true)
    try {
      const res = await embedSourceApi(sourceId)
      alert(`색인 완료: 성공 ${res.result.successCount}건, 실패 ${res.result.failCount}건`)
    } catch (error) {
      console.error(error)
      alert('색인 중 오류가 발생했습니다.')
    } finally {
      setIsEmbedding(false)
    }
  }

  const handleDeleteEmbed = async () => {
    if (!confirm('색인을 삭제하시겠습니까? 삭제된 색인은 복구할 수 없습니다.')) return
    setIsEmbedding(true)
    try {
      await deleteEmbedApi(sourceId)
      alert('색인이 삭제되었습니다.')
    } catch (error) {
      console.error(error)
      alert('색인 삭제 중 오류가 발생했습니다.')
    } finally {
      setIsEmbedding(false)
    }
  }

  function updateStatusBadge(updateState: string) {
    const colors: Record<string, string> = {
      'UPDATE-STATE-STAY': 'green',
      'UPDATE-STATE-INSERT': 'blue',
      'UPDATE-STATE-CHANGE': 'red',
      'UPDATE-STATE-DELETE': 'black',
    }
    const names: Record<string, string> = {
      'UPDATE-STATE-STAY': '변경없음',
      'UPDATE-STATE-INSERT': '추가',
      'UPDATE-STATE-CHANGE': '변경',
      'UPDATE-STATE-DELETE': '삭제',
    }
    const color = colors[updateState] || 'gray'
    const name = names[updateState] || '알수없음'
    return (
      <span className={`inline-flex items-center rounded-full bg-${color}-50 px-2 py-0.5 text-xs font-medium text-${color}-700 ring-1 ring-${color}-700/10 ring-inset`}>
        {name}
      </span>
    )
  }

  if (isLoading) {
    return (
      <div className="flex h-full w-full flex-col items-center justify-center gap-3">
        <Loader2 className="text-primary h-10 w-10 animate-spin" />
        <p className="text-sm font-medium text-gray-500">문서 정보를 불러오는 중입니다...</p>
      </div>
    )
  }

  if (error || !source) {
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

  const startIndex = (page - 1) * ITEMS_PER_PAGE

  return (
    <>
      {(isBatch || isEmbedding) && (
        <BatchLoadingModal message={isBatch ? '청킹 배치 작업 진행 중' : '색인 처리 진행 중'} />
      )}

      <div className="flex w-full flex-col p-6">
        <div className="mb-4 flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="flex items-center gap-2 text-2xl font-bold text-gray-800">
                <FolderOpen className="text-primary h-6 w-6" />
                패시지 목록
              </h2>
              <p className="mt-1 text-xs text-gray-500">대상 문서 상세 정보 & 패시지 목록</p>
            </div>

            <div className="flex items-center gap-3">
              {getRole() === 'ROLE_ADMIN' && (
                <>
                  <button
                    onClick={handleDeleteEmbed}
                    disabled={isEmbedding}
                    className="flex w-fit items-center gap-2 rounded-lg border border-red-300 bg-white px-4 py-2 text-sm font-bold text-red-600 shadow-sm transition-all hover:bg-red-50 active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    <Database className="h-4 w-4" />
                    색인 삭제
                  </button>
                  <button
                    onClick={handleEmbed}
                    disabled={isEmbedding}
                    className="bg-primary hover:bg-primary-hover flex w-fit items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold text-white shadow-sm transition-all active:scale-95 disabled:cursor-not-allowed disabled:bg-gray-300"
                  >
                    <Database className="h-4 w-4" />
                    색인
                  </button>
                  <button
                    onClick={handleChunkBatch}
                    disabled={isBatch}
                    className="bg-primary hover:bg-primary-hover flex w-fit items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold text-white shadow-sm transition-all active:scale-95 disabled:cursor-not-allowed disabled:bg-gray-300"
                  >
                    <RefreshCw className={`h-4 w-4 ${isBatch ? 'animate-spin' : ''}`} />
                    {isBatch ? '작업 중...' : '청킹배치'}
                  </button>
                </>
              )}
              <button
                onClick={() => router.back()}
                className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-bold text-gray-600 shadow-sm transition-colors hover:bg-gray-50 active:scale-95"
              >
                ← 뒤로가기
              </button>
            </div>
          </div>

          {/* 문서 상세 정보 카드 */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <h2 className="mb-4 text-2xl font-bold text-gray-800">{source.name}</h2>
            <div className="grid grid-cols-8 gap-4 text-sm text-gray-600">
              <div className="flex flex-col">
                <span className="pb-2 text-xs text-gray-400">문서 ID</span>
                <span className="font-bold text-gray-800">{source.sourceId}</span>
              </div>
              <div className="flex flex-col">
                <span className="pb-2 text-xs text-gray-400">버전</span>
                <span className="text-primary font-bold">v{source.version}</span>
              </div>
              <div className="flex flex-col">
                <span className="pb-2 text-xs text-gray-400">문서 타입</span>
                <span className="font-bold text-gray-800">{source.sourceTypeName}</span>
              </div>
              <div className="flex flex-col">
                <span className="pb-2 text-xs text-gray-400">문서 분류</span>
                <span className="font-bold text-gray-800">{source.categoryName}</span>
              </div>
              <div className="flex flex-col">
                <span className="pb-2 text-xs text-gray-400">전처리 타입</span>
                <span className="font-bold text-gray-800">{source.selectTypeName}</span>
              </div>
              <div className="flex flex-col">
                <span className="pb-2 text-xs text-gray-400">자동화여부</span>
                <div>
                  {source.isAuto ? (
                    <span className="inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-xs font-medium text-blue-700 ring-1 ring-blue-700/10 ring-inset">자동</span>
                  ) : (
                    <span className="inline-flex items-center rounded-full bg-yellow-50 px-2 py-0.5 text-xs font-medium text-yellow-800 ring-1 ring-yellow-600/20 ring-inset">수동</span>
                  )}
                </div>
              </div>
              <div className="flex flex-col">
                <span className="pb-2 text-xs text-gray-400">배치여부</span>
                <div>
                  {source.isBatch ? (
                    <span className="inline-flex items-center rounded-full bg-green-50 px-2 py-0.5 text-xs font-medium text-green-700 ring-1 ring-green-700/10 ring-inset">활성화</span>
                  ) : (
                    <span className="inline-flex items-center rounded-full bg-red-50 px-2 py-0.5 text-xs font-medium text-red-800 ring-1 ring-red-600/20 ring-inset">비활성화</span>
                  )}
                </div>
              </div>
              <div className="flex flex-col">
                <span className="pb-2 text-xs text-gray-400">등록일</span>
                <span className="font-bold text-gray-800">{source.sysCreateDt}</span>
              </div>
            </div>
          </div>
        </div>

        {/* 패시지 목록 테이블 */}
        <div className="flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
          <div className="overflow-auto">
            <table className="w-full min-w-full text-left text-sm text-gray-600">
              <thead className="bg-gray-50 text-xs font-bold text-gray-500 uppercase shadow-sm">
                <tr>
                  <th className="w-[80px] px-6 py-4 text-center">ID</th>
                  <th className="w-[80px] px-6 py-4 text-center">버전</th>
                  <th className="w-[150px] px-6 py-4">제목</th>
                  <th className="px-6 py-4">본문</th>
                  <th className="w-[80px] px-6 py-4 text-center">토큰</th>
                  <th className="w-[110px] px-6 py-4 text-center">변경이력</th>
                  <th className="w-[80px] px-6 py-4 text-center">순서</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 bg-white">
                {passageList.map((passage) => (
                  <tr
                    key={passage.passageId}
                    onClick={() => router.push(`/passage/detail?passageId=${passage.passageId}`)}
                    className="group cursor-pointer transition-colors hover:bg-gray-50"
                  >
                    <td className="px-6 py-4 text-center font-mono text-gray-400 group-hover:text-gray-600">{passage.passageId}</td>
                    <td className="px-6 py-4 text-center text-xs text-gray-400">v{passage.version}</td>
                    <td className="px-6 py-4">
                      <div className="flex flex-col truncate">
                        <span className="group-hover:text-primary font-bold text-gray-800 transition-colors">{passage.title}</span>
                        {passage.subTitle && <span className="text-xs text-gray-500">{passage.subTitle}</span>}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="max-w-2xl truncate text-gray-600 group-hover:text-gray-900" title={passage.content}>{passage.content}</div>
                      {passage.subContent && (
                        <div className="mt-1 max-w-xl truncate text-xs text-gray-400" title={passage.subContent}>↳ {passage.subContent}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className="inline-flex items-center rounded-md bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">{passage.contentTokenSize}</span>
                    </td>
                    <td className="px-6 py-4 text-center">{updateStatusBadge(passage.updateState)}</td>
                    <td className="px-6 py-4 text-center font-medium text-gray-600">{passage.sortOrder}</td>
                  </tr>
                ))}
                {passageList.length === 0 && (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-gray-500">생성된 Passage가 없습니다.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between border-t border-gray-100 bg-gray-50 px-6 py-3">
            <span className="text-xs text-gray-500">
              패시지 목록 <span className="font-bold">{Math.min(startIndex + 1, totalCounts)}</span>~<span className="font-bold">{Math.min(startIndex + ITEMS_PER_PAGE, totalCounts)}</span>
              {' (전체 '}<span className="font-bold">{totalCounts}</span>{' 개의 패시지)'}
            </span>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={page === 1}
                className="rounded border border-gray-300 bg-white px-3 py-1 text-xs font-medium text-gray-600 shadow-sm hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                이전
              </button>
              <span className="px-2 text-xs font-bold text-gray-700">{page} / {totalPages}</span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                disabled={page === totalPages}
                className="rounded border border-gray-300 bg-white px-3 py-1 text-xs font-medium text-gray-600 shadow-sm hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                다음
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default function SourceDetailPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-screen items-center justify-center">
          <Loader2 className="h-10 w-10 animate-spin text-blue-500" />
        </div>
      }
    >
      <SourceDetailContent />
    </Suspense>
  )
}
