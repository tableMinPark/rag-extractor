'use client'

import React, { useEffect, useState } from 'react'
import {
  Search,
  Filter,
  FileText,
  LayoutTemplate,
  ChevronLeft,
  ChevronRight,
  Loader2,
  X,
  Maximize2,
  Type,
} from 'lucide-react'
import { Category } from '@/types/domain'
import { getCategoriesSourceApi } from '@/api/source'

// ###################################################
// [타입 정의]
// ###################################################
interface SearchResultItem {
  id: string
  score: number // BM25 Score
  categoryCode: string
  title: string
  subTitle: string
  content: string
  subContent: string
  createDt: string
}

export default function KeywordSearchPage() {
  // ###################################################
  // [상태 관리]
  // ###################################################
  const [query, setQuery] = useState('')
  const [categories, setCategories] = useState<Category[]>([])
  const [categoryCode, setCategoryCode] = useState('ALL')

  const [results, setResults] = useState<SearchResultItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)

  // 페이지네이션
  const [page, setPage] = useState(1)
  const [totalCount, setTotalCount] = useState(0)
  const ITEMS_PER_PAGE = 10

  // 상세 모달
  const [selectedItem, setSelectedItem] = useState<SearchResultItem | null>(
    null,
  )
  const [isModalOpen, setIsModalOpen] = useState(false)

  const loadData = async () => {
    setIsLoading(true)
    try {
      await getCategoriesSourceApi().then((response) => {
        console.log(`📡 ${response.message}`)
        setCategories(() => response.result)
      })
    } catch (err) {
      console.error(err)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  // ###################################################
  // [핸들러]
  // ###################################################
  const handleSearch = async (newPage = 1) => {
    if (!query.trim()) {
      alert('검색어를 입력해주세요.')
      return
    }

    setIsLoading(true)
    setHasSearched(true)
    setPage(newPage)

    try {
      // TODO: 변경 필요 => 검색 결과 호출
      await new Promise((resolve) => setTimeout(resolve, 500))

      // [Mock Data] 키워드 검색 결과
      const mockData: SearchResultItem[] = Array.from({
        length: ITEMS_PER_PAGE,
      }).map((_, i) => ({
        id: `kw-doc-${Date.now()}-${i}`,
        // BM25 점수 예시
        score: Number((15.5 - i * 0.8 - Math.random() * 0.5).toFixed(2)),
        categoryCode: categories[i % categories.length].code,
        title: `업무 가이드라인 제${10 + i}장 (키워드: ${query})`,
        subTitle: `제${i + 1}절 세부사항`,
        content: `검색어 "${query}"가 포함된 텍스트입니다. 키워드 검색은 단어의 빈도수와 역문서 빈도(TF-IDF) 등을 고려하여 점수를 매깁니다.\n이 문서는 사용자가 입력한 키워드와 정확히 일치하는 용어를 포함하고 있을 확률이 높습니다.`,
        subContent: `등록일: 2024-03-${String(i + 1).padStart(2, '0')} \n작성자: 관리자`,
        createDt: '2024-03-20',
      }))

      setResults(mockData)
      setTotalCount(32)
    } catch (error) {
      console.error(error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleSearch(1)
  }

  const handlePageChange = (newPage: number) => {
    if (newPage < 1 || newPage > Math.ceil(totalCount / ITEMS_PER_PAGE)) return
    handleSearch(newPage)
  }

  const openDetail = (item: SearchResultItem) => {
    setSelectedItem(item)
    setIsModalOpen(true)
  }

  // ###################################################
  // [렌더링]
  // ###################################################
  const totalPages = Math.ceil(totalCount / ITEMS_PER_PAGE)

  return (
    <div className="flex h-full w-full flex-col p-6">
      {/* 1. 헤더 영역 */}
      <div className="mb-6 flex items-center gap-3">
        {/* 아이콘 색상 Primary로 통일 */}
        <Type className="text-primary h-8 w-8" />
        <div>
          <h2 className="text-2xl font-bold text-gray-800">
            키워드 검색 (Keyword Search)
          </h2>
          <p className="mt-1 text-xs text-gray-500">
            형태소 분석을 통해 키워드가 정확히 일치하거나 포함된 문서를
            검색합니다.
          </p>
        </div>
      </div>

      {/* 2. 검색 바 영역 */}
      <div className="mb-6 flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
        <div className="flex gap-3">
          {/* 카테고리 선택 */}
          <div className="relative shrink-0">
            <select
              value={categoryCode}
              onChange={(e) => setCategoryCode(e.target.value)}
              className="focus:border-primary focus:ring-primary h-12 w-[140px] cursor-pointer appearance-none rounded-lg border border-gray-200 bg-gray-50 px-4 text-sm font-bold text-gray-700 transition-all outline-none focus:bg-white focus:ring-1"
            >
              <option value="ALL">전체</option>
              {categories.map((cat) => (
                <option key={cat.code} value={cat.code}>
                  {cat.name}
                </option>
              ))}
            </select>
            <Filter className="pointer-events-none absolute top-1/2 right-3 h-4 w-4 -translate-y-1/2 text-gray-400" />
          </div>

          {/* 검색어 입력 */}
          <div className="relative flex-1">
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="찾으시는 단어나 문장을 입력하세요..."
              className="focus:border-primary focus:ring-primary h-12 w-full rounded-lg border border-gray-200 bg-white pr-4 pl-11 text-sm transition-all outline-none placeholder:text-gray-400 focus:ring-1"
            />
            <Search className="absolute top-1/2 left-4 h-5 w-5 -translate-y-1/2 text-gray-400" />
          </div>

          {/* 검색 버튼 (Primary 색상) */}
          <button
            onClick={() => handleSearch(1)}
            className="bg-primary hover:bg-primary-hover flex h-12 w-24 shrink-0 items-center justify-center rounded-lg text-sm font-bold text-white shadow-md transition-all active:scale-95"
          >
            검색
          </button>
        </div>
      </div>

      {/* 3. 검색 결과 목록 */}
      <div className="flex flex-1 flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        {/* 결과 헤더 */}
        <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-6 py-4">
          <span className="text-sm font-bold text-gray-700">검색 결과</span>
          {hasSearched && (
            <span className="text-xs text-gray-500">
              총 <span className="text-primary font-bold">{totalCount}</span>{' '}
              건의 문서가 검색되었습니다.
            </span>
          )}
        </div>

        {/* 리스트 영역 */}
        <div className="scrollbar-thin scrollbar-thumb-gray-200 flex-1 overflow-y-auto bg-gray-50/30 p-6">
          {isLoading ? (
            <div className="flex h-full flex-col items-center justify-center gap-3 text-gray-400">
              <Loader2 className="text-primary h-8 w-8 animate-spin" />
              <p className="text-sm">키워드를 찾고 있습니다...</p>
            </div>
          ) : !hasSearched ? (
            <div className="flex h-full flex-col items-center justify-center gap-4 text-gray-300">
              <Search className="h-12 w-12 opacity-20" />
              <p className="text-sm font-medium text-gray-400">
                검색어를 입력하여 정확한 문서를 찾아보세요.
              </p>
            </div>
          ) : results.length === 0 ? (
            <div className="flex h-full flex-col items-center justify-center gap-4 text-gray-300">
              <X className="h-12 w-12 opacity-20" />
              <p className="text-sm font-medium text-gray-400">
                일치하는 결과가 없습니다.
              </p>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {results.map((item) => (
                <div
                  key={item.id}
                  onClick={() => openDetail(item)}
                  className="group hover:border-primary/50 relative flex cursor-pointer flex-col gap-3 rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-all hover:shadow-md"
                >
                  {/* 상단: 타이틀 & 점수 & 카테고리 */}
                  <div className="flex items-start justify-between">
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <span className="rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-bold text-gray-500">
                          {item.categoryCode}
                        </span>
                        <h4 className="group-hover:text-primary line-clamp-1 text-sm font-bold text-gray-900 transition-colors">
                          {item.title}
                        </h4>
                      </div>
                      <span className="text-xs text-gray-500">
                        {item.subTitle}
                      </span>
                    </div>

                    {/* Score Badge (색상 테마를 벡터 검색과 통일) */}
                    <div
                      className={`flex shrink-0 items-center gap-1 rounded-md px-2 py-1 text-xs font-bold ${
                        item.score >= 10
                          ? 'border border-green-100 bg-green-50 text-green-700'
                          : item.score >= 5
                            ? 'border border-yellow-100 bg-yellow-50 text-yellow-700'
                            : 'border border-gray-100 bg-gray-50 text-gray-600'
                      }`}
                    >
                      <span className="mr-1 text-[10px] text-gray-400 uppercase">
                        Match
                      </span>
                      {item.score}
                    </div>
                  </div>

                  {/* 본문 (2줄 제한) */}
                  <div className="border-l-2 border-gray-100 pl-3">
                    <p className="line-clamp-2 text-xs leading-relaxed text-gray-700">
                      {item.content}
                    </p>
                  </div>

                  {/* 부가 정보 (2줄 제한) */}
                  {item.subContent && (
                    <div className="rounded bg-gray-50 p-2.5">
                      <p className="line-clamp-2 font-mono text-[11px] leading-relaxed text-gray-500">
                        {item.subContent}
                      </p>
                    </div>
                  )}

                  {/* 호버 아이콘 */}
                  <div className="absolute right-5 bottom-5 opacity-0 transition-opacity group-hover:opacity-100">
                    <Maximize2 className="text-primary h-4 w-4" />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 4. 페이지네이션 */}
        {hasSearched && totalCount > 0 && (
          <div className="flex items-center justify-between border-t border-gray-100 bg-white px-6 py-3">
            <span className="text-xs text-gray-500">
              Page <span className="font-bold text-gray-800">{page}</span> of{' '}
              {totalPages}
            </span>
            <div className="flex items-center gap-1">
              <button
                onClick={() => handlePageChange(page - 1)}
                disabled={page === 1}
                className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>

              <div className="flex items-center px-2">
                {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => {
                  const pNum = i + 1
                  return (
                    <button
                      key={pNum}
                      onClick={() => handlePageChange(pNum)}
                      className={`h-8 w-8 rounded-lg text-xs font-bold transition-colors ${
                        page === pNum
                          ? 'bg-primary text-white'
                          : 'text-gray-600 hover:bg-gray-100'
                      }`}
                    >
                      {pNum}
                    </button>
                  )
                })}
              </div>

              <button
                onClick={() => handlePageChange(page + 1)}
                disabled={page === totalPages}
                className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* ################################################### */}
      {/* [모달] 상세 보기 */}
      {/* ################################################### */}
      {isModalOpen && selectedItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
          <div className="animate-in zoom-in-95 flex h-[80vh] w-full max-w-3xl flex-col overflow-hidden rounded-2xl bg-white shadow-2xl duration-200">
            {/* 모달 헤더 */}
            <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
              <div className="flex items-center gap-3">
                <span className="rounded bg-gray-100 px-2 py-0.5 text-xs font-bold text-gray-600">
                  {selectedItem.categoryCode}
                </span>
                <h3 className="line-clamp-1 text-lg font-bold text-gray-800">
                  {selectedItem.title}
                </h3>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="rounded-full p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            {/* 모달 내용 */}
            <div className="scrollbar-thin scrollbar-thumb-gray-300 flex-1 overflow-y-auto p-6">
              <div className="flex flex-col gap-6">
                {/* 메타 정보 */}
                <div className="rounded-lg border border-gray-100 bg-gray-50 p-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">
                        Match Score
                      </span>
                      <span className="text-primary text-sm font-bold">
                        {selectedItem.score}
                      </span>
                    </div>
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">
                        ID
                      </span>
                      <span className="font-mono text-xs text-gray-600">
                        {selectedItem.id}
                      </span>
                    </div>
                    <div className="col-span-2">
                      <span className="mb-1 block text-xs font-bold text-gray-400">
                        Sub Title
                      </span>
                      <span className="text-sm text-gray-700">
                        {selectedItem.subTitle}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Main Content */}
                <div>
                  <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                    <FileText className="text-primary h-4 w-4" /> Main Content
                  </h4>
                  <div className="rounded-lg border border-gray-200 bg-white p-5 text-sm leading-8 whitespace-pre-wrap text-gray-800">
                    {selectedItem.content}
                  </div>
                </div>

                {/* Sub Content */}
                {selectedItem.subContent && (
                  <div>
                    <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                      <LayoutTemplate className="text-primary h-4 w-4" /> Sub
                      Content
                    </h4>
                    <div className="rounded-lg border border-gray-200 bg-gray-50/50 p-5 font-mono text-sm leading-7 whitespace-pre-wrap text-gray-600">
                      {selectedItem.subContent}
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* 모달 하단 */}
            <div className="flex justify-end gap-2 border-t border-gray-100 bg-gray-50 px-6 py-4">
              <button
                onClick={() => setIsModalOpen(false)}
                className="rounded-lg border border-gray-300 bg-white px-5 py-2 text-sm font-bold text-gray-700 shadow-sm hover:bg-gray-50"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
