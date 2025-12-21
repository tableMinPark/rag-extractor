'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter } from 'next/navigation'
import { FolderOpen, Loader2, AlertCircle, RefreshCw } from 'lucide-react'
import { Category, Source } from '@/types/domain'
import { getCategoriesSourceApi, getSourcesApi } from '@/api/source'
import { CreateSourceModal } from '@/components/modal/CreateSourceModal'

export default function SourceListPage() {
  const router = useRouter()
  const ITEM_INIT_PAGE = 1
  const ITEMS_PER_PAGE = 10

  // ###################################################
  // 상태 정의 (State)
  // ###################################################
  const [sourceList, setSourceList] = useState<Source[]>([])
  const [page, setPage] = useState(ITEM_INIT_PAGE)
  const [size, setSize] = useState(ITEMS_PER_PAGE)
  const [totalPages, setTotalPages] = useState(0)
  const [totalCounts, setTotalCounts] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [keyword, setKeyword] = useState('')
  const [categories, setCategories] = useState<Category[]>([])
  const [selectedFilterCategory, setSelectedFilterCategory] = useState('ALL')

  const loadData = async () => {
    setIsLoading(true)
    setError(null)
    try {
      await getSourcesApi(page, size, keyword, selectedFilterCategory).then(
        (response) => {
          console.log(`📡 ${response.message}`)
          setPage(response.result.pageNo)
          setSize(response.result.pageSize)
          setTotalPages(response.result.totalPages)
          setTotalCounts(response.result.totalCount)
          setSourceList(response.result.content)
        },
      )
      await getCategoriesSourceApi().then((response) => {
        console.log(`📡 ${response.message}`)
        setCategories(() => response.result)
      })
    } catch (err) {
      console.error(err)
      setError('문서 목록을 불러올 수 없습니다.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [page])

  useEffect(() => {
    loadData()
  }, [])

  // ###################################################
  // 핸들러 (Handler)
  // ###################################################
  const startIndex = (page - 1) * ITEMS_PER_PAGE
  const endIndex = startIndex + ITEMS_PER_PAGE

  const handleRowClick = (sourceId: number) => {
    router.push(`/source/detail?sourceId=${sourceId}`)
  }

  const handlePrevPage = () => {
    if (page > 1) {
      setPage((prev) => prev - 1)
    }
  }

  const handleNextPage = () => {
    if (page < totalPages) {
      setPage((prev) => prev + 1)
    }
  }

  const handleRefresh = () => {
    setPage((prev) => {
      if (prev == ITEM_INIT_PAGE) {
        loadData()
      }
      return ITEM_INIT_PAGE
    })
  }

  const handleRegisterSuccess = () => {
    setPage((prev) => {
      if (prev == ITEM_INIT_PAGE) {
        loadData()
      }
      return ITEM_INIT_PAGE
    })
  }

  // ###################################################
  // 렌더링 (Render)
  // ###################################################
  return (
    <div className="flex w-full flex-col p-6">
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div>
            <h2 className="flex items-center gap-2 text-2xl font-bold text-gray-800">
              <FolderOpen className="text-primary h-6 w-6" />
              RAG 문서 관리
            </h2>
            <p className="mt-1 text-xs text-gray-500">대상 문서 목록</p>
          </div>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-primary hover:bg-primary-hover flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold text-white shadow-sm transition-all active:scale-95"
        >
          <span>+ 문서 등록</span>
        </button>
      </div>
      {/* 2. 검색 및 필터 영역 (새로 추가됨) */}
      <div className="mb-4 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
        {/* 좌측: 카테고리 셀렉트 + 검색어 입력 */}
        <div className="flex flex-1 items-center gap-3">
          {/* 카테고리 필터 셀렉트 */}
          <div className="relative">
            <select
              value={selectedFilterCategory}
              onChange={(e) => setSelectedFilterCategory(e.target.value)}
              className="focus:border-primary focus:ring-primary h-10 w-[140px] appearance-none rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 focus:ring-1 focus:outline-none"
            >
              <option value="ALL">전체 카테고리</option>
              {categories.map((cat) => (
                <option key={cat.code} value={cat.code}>
                  {cat.name}
                </option>
              ))}
            </select>
            {/* 셀렉트 화살표 아이콘 커스텀 */}
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-500">
              <svg className="h-4 w-4 fill-current" viewBox="0 0 20 20">
                <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" />
              </svg>
            </div>
          </div>

          {/* 검색어 입력창 */}
          <div className="relative max-w-md flex-1">
            <input
              type="text"
              placeholder="문서명을 검색하세요..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleRefresh()} // 엔터키 검색
              className="focus:border-primary focus:ring-primary h-10 w-full rounded-lg border border-gray-300 pr-4 pl-10 text-sm focus:ring-1 focus:outline-none"
            />
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-5 w-5 text-gray-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </div>
          </div>

          {/* 검색 버튼 (선택 사항) */}
          <button
            onClick={handleRefresh}
            className="hover:text-primary h-10 rounded-lg border border-gray-300 bg-gray-50 px-4 text-sm font-bold text-gray-600 transition-colors hover:bg-gray-100"
          >
            검색
          </button>
        </div>
      </div>

      <div className="flex min-h-[400px] flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        {isLoading && (
          <div className="flex flex-1 flex-col items-center justify-center gap-3">
            <Loader2 className="text-primary h-8 w-8 animate-spin" />
            <p className="text-sm font-medium text-gray-500">
              목록을 불러오는 중입니다...
            </p>
          </div>
        )}

        {!isLoading && error && (
          <div className="flex flex-1 flex-col items-center justify-center gap-3">
            <AlertCircle className="h-8 w-8 text-red-500" />
            <p className="text-sm font-bold text-gray-700">{error}</p>
            <button
              onClick={handleRefresh}
              className="flex items-center gap-2 rounded-md bg-gray-100 px-3 py-1.5 text-xs font-bold text-gray-600 hover:bg-gray-200"
            >
              <RefreshCw className="h-3 w-3" />
              다시 시도
            </button>
          </div>
        )}

        {!isLoading && !error && (
          <>
            <div className="overflow-auto">
              <table className="w-full min-w-full text-left text-sm text-gray-600">
                <thead className="bg-gray-50 text-xs font-bold text-gray-500 uppercase shadow-sm">
                  <tr>
                    <th className="w-[60px] px-4 py-4 text-center">ID</th>
                    <th className="px-4 py-4">문서명</th>
                    <th className="w-[80px] px-4 py-4 text-center">분류</th>
                    <th className="w-[80px] px-4 py-4 text-center">타입</th>
                    <th className="w-[80px] px-4 py-4 text-center">전처리</th>
                    <th className="w-[60px] px-4 py-4 text-center">버전</th>
                    <th className="w-[80px] px-4 py-4 text-center">자동화</th>
                    <th className="w-[100px] px-4 py-4 text-center">
                      배치여부
                    </th>
                    <th className="w-[180px] px-4 py-4 text-center">생성일</th>
                    <th className="w-[180px] px-4 py-4 text-center">수정일</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 bg-white">
                  {sourceList.map((source) => (
                    <tr
                      key={source.sourceId}
                      onClick={() => handleRowClick(source.sourceId)}
                      className="group cursor-pointer transition-colors hover:bg-gray-50"
                    >
                      <td className="px-4 py-4 text-center font-mono text-gray-400 group-hover:text-gray-600">
                        {source.sourceId}
                      </td>
                      <td
                        className="group-hover:text-primary max-w-[200px] truncate px-4 py-4 font-bold text-gray-800 transition-colors"
                        title={source.name}
                      >
                        {source.name}
                      </td>
                      <td className="px-4 py-4 text-center">
                        <span className="inline-flex items-center rounded-md bg-gray-100 px-2 py-1 text-xs font-medium text-gray-600">
                          {source.categoryName}
                        </span>
                      </td>
                      <td className="px-4 py-4 text-center">
                        <span className="font-mono text-xs font-bold text-gray-500">
                          {source.sourceTypeName}
                        </span>
                      </td>
                      <td className="px-4 py-4 text-center">
                        <span className="text-xs text-gray-600">
                          {source.selectTypeName}
                        </span>
                      </td>
                      <td className="px-4 py-4 text-center text-gray-500">
                        v{source.version}
                      </td>
                      <td className="px-4 py-4 text-center">
                        {source.isAuto ? (
                          <span className="inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-xs font-medium text-blue-700 ring-1 ring-blue-700/10 ring-inset">
                            자동
                          </span>
                        ) : (
                          <span className="inline-flex items-center rounded-full bg-yellow-50 px-2 py-0.5 text-xs font-medium text-yellow-800 ring-1 ring-yellow-600/20 ring-inset">
                            수동
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-4 text-center">
                        {source.isBatch ? (
                          <span className="inline-flex items-center rounded-full bg-green-50 px-2 py-0.5 text-xs font-medium text-green-700 ring-1 ring-green-700/10 ring-inset">
                            활성화
                          </span>
                        ) : (
                          <span className="inline-flex items-center rounded-full bg-red-50 px-2 py-0.5 text-xs font-medium text-red-800 ring-1 ring-red-600/20 ring-inset">
                            비활성화
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-4 text-center text-xs text-gray-500">
                        {source.sysCreateDt}
                      </td>
                      <td className="px-4 py-4 text-center text-xs text-gray-500">
                        {source.sysModifyDt}
                      </td>
                    </tr>
                  ))}
                  {sourceList.length === 0 && (
                    <tr>
                      <td
                        colSpan={10}
                        className="px-6 py-12 text-center text-gray-500"
                      >
                        등록된 문서가 없습니다.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
            <div className="mt-auto flex items-center justify-between border-t border-gray-100 bg-gray-50 px-6 py-3">
              <span className="text-xs text-gray-500">
                문서 목록 <span className="font-bold">{startIndex + 1}</span> ~{' '}
                <span className="font-bold">
                  {Math.min(endIndex, totalCounts)}
                </span>{' '}
                {'(전체 '}
                <span className="font-bold">{totalCounts}</span> {' 개의 문서)'}
              </span>
              <div className="flex items-center gap-2">
                <button
                  onClick={handlePrevPage}
                  disabled={page === 1}
                  className="rounded border border-gray-300 bg-white px-3 py-1 text-xs font-medium text-gray-600 shadow-sm hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  이전
                </button>
                <span className="px-2 text-xs font-bold text-gray-700">
                  {page} / {totalPages}
                </span>
                <button
                  onClick={handleNextPage}
                  disabled={page === totalPages}
                  className="rounded border border-gray-300 bg-white px-3 py-1 text-xs font-medium text-gray-600 shadow-sm hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  다음
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      <CreateSourceModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={handleRegisterSuccess}
      />
    </div>
  )
}
