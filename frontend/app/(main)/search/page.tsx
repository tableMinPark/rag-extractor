'use client'

import React, { useCallback, useEffect, useState } from 'react'
import {
  ChevronLeft,
  ChevronRight,
  FileText,
  Layers,
  LayoutTemplate,
  Loader2,
  Maximize2,
  Search,
  SearchCode,
  X,
} from 'lucide-react'
import {
  getCollectionsApi,
  getSearchCategoriesApi,
  keywordSearchApi,
  vectorSearchApi,
  SearchCollection,
  SearchCategory,
  SearchResultItem,
  SearchRequestBody,
} from '@/api/search'

// ─── 상수 ────────────────────────────────────────────────
type SearchMode = 'KEYWORD' | 'VECTOR' | 'HYBRID'
const TOP_K = 10
const SEARCH_MODE_OPTIONS: { value: SearchMode; label: string }[] = [
  { value: 'KEYWORD', label: '키워드 검색' },
  { value: 'VECTOR', label: '벡터 검색' },
  { value: 'HYBRID', label: '키워드 + 벡터 (하이브리드)' },
]

// ─── 유틸 ────────────────────────────────────────────────
function deduplicateByChunkId(items: SearchResultItem[]): SearchResultItem[] {
  const seen = new Set<number>()
  return items.filter((item) => {
    if (seen.has(item.chunkId)) return false
    seen.add(item.chunkId)
    return true
  })
}

// ─── 컴포넌트 ────────────────────────────────────────────
export default function SearchPage() {
  // 초기 데이터
  const [collections, setCollections] = useState<SearchCollection[]>([])
  const [categories, setCategories] = useState<SearchCategory[]>([])

  // 검색 설정
  const [collectionId, setCollectionId] = useState('')
  const [selectedCodes, setSelectedCodes] = useState<string[]>([])
  const [searchMode, setSearchMode] = useState<SearchMode>('KEYWORD')
  const [useRerank, setUseRerank] = useState(false)

  // 검색 상태
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<SearchResultItem[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)

  // 페이지네이션
  const [page, setPage] = useState(1)
  const ITEMS_PER_PAGE = 10
  const pagedResults = results.slice((page - 1) * ITEMS_PER_PAGE, page * ITEMS_PER_PAGE)
  const totalPages = Math.ceil(results.length / ITEMS_PER_PAGE)

  // 모달
  const [selectedItem, setSelectedItem] = useState<SearchResultItem | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)

  // ─── 초기 로드 ──────────────────────────────────────────
  useEffect(() => {
    let cancelled = false
    const load = async () => {
      try {
        const [colRes, catRes] = await Promise.all([
          getCollectionsApi(),
          getSearchCategoriesApi(),
        ])
        if (cancelled) return
        setCollections(colRes.result)
        setCategories(catRes.result)
        if (colRes.result.length > 0) setCollectionId(colRes.result[0].collectionId)
        // 전체 카테고리 기본 선택
        setSelectedCodes(catRes.result.map((c) => c.code))
      } catch (err) {
        if (!cancelled) console.error(err)
      }
    }
    load()
    return () => { cancelled = true }
  }, [])

  // ─── 카테고리 토글 ──────────────────────────────────────
  const toggleCategory = (code: string) => {
    setSelectedCodes((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code],
    )
  }

  const toggleAllCategories = () => {
    if (selectedCodes.length === categories.length) {
      setSelectedCodes([])
    } else {
      setSelectedCodes(categories.map((c) => c.code))
    }
  }

  // ─── 검색 실행 ──────────────────────────────────────────
  const handleSearch = useCallback(async () => {
    if (!query.trim()) {
      alert('검색어를 입력해주세요.')
      return
    }
    if (!collectionId) {
      alert('컬렉션을 선택해주세요.')
      return
    }

    setIsLoading(true)
    setHasSearched(true)
    setPage(1)

    const body: SearchRequestBody = {
      query,
      collectionId,
      topK: TOP_K,
      // 전체 선택이면 빈 배열(필터 없음), 일부 선택이면 해당 코드 목록
      aliases: selectedCodes.length === categories.length ? [] : selectedCodes,
    }

    try {
      let merged: SearchResultItem[] = []

      if (searchMode === 'KEYWORD') {
        const res = await keywordSearchApi(body)
        merged = res.result
      } else if (searchMode === 'VECTOR') {
        const res = await vectorSearchApi(body)
        merged = res.result
      } else {
        // HYBRID: 병렬 호출 후 합산, chunkId 기준 중복 제거
        const [kwRes, vecRes] = await Promise.all([
          keywordSearchApi(body),
          vectorSearchApi(body),
        ])
        merged = deduplicateByChunkId([...kwRes.result, ...vecRes.result])
          .sort((a, b) => b.score - a.score)
      }

      // TODO: useRerank === true 일 때 /search/rerank 호출 (추후 구현)
      if (useRerank) {
        console.log('[리랭킹] 추후 구현 예정 - 현재는 원본 결과 표시')
      }

      setResults(merged)
    } catch (err) {
      console.error(err)
      setResults([])
    } finally {
      setIsLoading(false)
    }
  }, [query, collectionId, selectedCodes, categories, searchMode, useRerank])

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleSearch()
  }

  return (
    <div className="flex h-full w-full flex-col gap-4 p-6">

      {/* ── 헤더 ── */}
      <div className="flex items-center gap-3">
        <SearchCode className="text-primary h-8 w-8" />
        <div>
          <h2 className="text-2xl font-bold text-gray-800">RAG 검색</h2>
          <p className="mt-1 text-xs text-gray-500">
            키워드·벡터·하이브리드 검색으로 청크를 탐색합니다.
          </p>
        </div>
      </div>

      {/* ── 검색 설정 패널 ── */}
      <div className="flex flex-col gap-4 rounded-xl border border-gray-200 bg-white p-5 shadow-sm">

        {/* 컬렉션 + 검색 모드 */}
        <div className="flex flex-wrap items-center gap-6">
          {/* 컬렉션 */}
          <div className="flex flex-col gap-1">
            <span className="text-xs font-bold text-gray-500">컬렉션</span>
            <select
              value={collectionId}
              onChange={(e) => setCollectionId(e.target.value)}
              className="focus:border-primary focus:ring-primary h-10 w-48 cursor-pointer appearance-none rounded-lg border border-gray-200 bg-gray-50 px-3 text-sm font-bold text-gray-700 outline-none focus:bg-white focus:ring-1"
            >
              {collections.map((col) => (
                <option key={col.collectionId} value={col.collectionId}>
                  {col.collectionName}
                </option>
              ))}
            </select>
          </div>

          {/* 검색 모드 라디오 */}
          <div className="flex flex-col gap-1">
            <span className="text-xs font-bold text-gray-500">검색 모드</span>
            <div className="flex items-center gap-4">
              {SEARCH_MODE_OPTIONS.map((opt) => (
                <label key={opt.value} className="flex cursor-pointer items-center gap-1.5">
                  <input
                    type="radio"
                    name="searchMode"
                    value={opt.value}
                    checked={searchMode === opt.value}
                    onChange={() => setSearchMode(opt.value)}
                    className="text-primary accent-primary"
                  />
                  <span className="text-sm text-gray-700">{opt.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* 리랭킹 */}
          <div className="flex flex-col gap-1">
            <span className="text-xs font-bold text-gray-500">리랭킹</span>
            <label className="flex cursor-pointer items-center gap-1.5">
              <input
                type="checkbox"
                checked={useRerank}
                onChange={(e) => setUseRerank(e.target.checked)}
                className="text-primary accent-primary h-4 w-4"
              />
              <span className="text-sm text-gray-700">리랭킹 적용</span>
            </label>
          </div>
        </div>

        {/* 카테고리 다중 선택 */}
        <div className="flex flex-col gap-2">
          <div className="flex items-center gap-3">
            <span className="text-xs font-bold text-gray-500">카테고리</span>
            <button
              onClick={toggleAllCategories}
              className="text-primary text-xs font-bold hover:underline"
            >
              {selectedCodes.length === categories.length ? '전체 해제' : '전체 선택'}
            </button>
          </div>
          <div className="flex flex-wrap gap-2">
            {categories.map((cat) => (
              <label
                key={cat.code}
                className={`flex cursor-pointer items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-bold transition-colors ${
                  selectedCodes.includes(cat.code)
                    ? 'border-primary bg-primary/10 text-primary'
                    : 'border-gray-200 bg-gray-50 text-gray-500 hover:bg-gray-100'
                }`}
              >
                <input
                  type="checkbox"
                  checked={selectedCodes.includes(cat.code)}
                  onChange={() => toggleCategory(cat.code)}
                  className="hidden"
                />
                {cat.name}
              </label>
            ))}
          </div>
        </div>

        {/* 검색 바 */}
        <div className="flex gap-3">
          <div className="relative flex-1">
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="찾으시는 단어나 문장을 입력하세요..."
              className="focus:border-primary focus:ring-primary h-12 w-full rounded-lg border border-gray-200 bg-white pr-4 pl-11 text-sm outline-none placeholder:text-gray-400 focus:ring-1"
            />
            <Search className="absolute top-1/2 left-4 h-5 w-5 -translate-y-1/2 text-gray-400" />
          </div>
          <button
            onClick={handleSearch}
            disabled={isLoading}
            className="bg-primary hover:bg-primary/90 flex h-12 w-24 shrink-0 items-center justify-center rounded-lg text-sm font-bold text-white shadow-md transition-all active:scale-95 disabled:opacity-60"
          >
            {isLoading ? <Loader2 className="h-5 w-5 animate-spin" /> : '검색'}
          </button>
        </div>
      </div>

      {/* ── 결과 영역 ── */}
      <div className="flex flex-1 flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        {/* 결과 헤더 */}
        <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-6 py-4">
          <span className="flex items-center gap-2 text-sm font-bold text-gray-700">
            <Layers className="h-4 w-4" />
            검색 결과
          </span>
          {hasSearched && (
            <span className="text-xs text-gray-500">
              총 <span className="text-primary font-bold">{results.length}</span>건
            </span>
          )}
        </div>

        {/* 리스트 */}
        <div className="scrollbar-thin scrollbar-thumb-gray-200 flex-1 overflow-y-auto bg-gray-50/30 p-6">
          {isLoading ? (
            <div className="flex h-full flex-col items-center justify-center gap-3 text-gray-400">
              <Loader2 className="text-primary h-8 w-8 animate-spin" />
              <p className="text-sm">검색 중...</p>
            </div>
          ) : !hasSearched ? (
            <div className="flex h-full flex-col items-center justify-center gap-4">
              <Search className="h-12 w-12 text-gray-200" />
              <p className="text-sm font-medium text-gray-400">
                검색어를 입력하여 문서를 탐색하세요.
              </p>
            </div>
          ) : pagedResults.length === 0 ? (
            <div className="flex h-full flex-col items-center justify-center gap-4">
              <X className="h-12 w-12 text-gray-200" />
              <p className="text-sm font-medium text-gray-400">일치하는 결과가 없습니다.</p>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {pagedResults.map((item) => (
                <div
                  key={item.chunkId}
                  onClick={() => { setSelectedItem(item); setIsModalOpen(true) }}
                  className="group hover:border-primary/50 relative flex cursor-pointer flex-col gap-3 rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-all hover:shadow-md"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex min-w-0 flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <span className="shrink-0 rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-bold text-gray-500">
                          {item.categoryCode}
                        </span>
                        <h4 className="group-hover:text-primary line-clamp-1 text-sm font-bold text-gray-900 transition-colors">
                          {item.title || item.name}
                        </h4>
                      </div>
                      {item.subTitle && (
                        <span className="text-xs text-gray-500">{item.subTitle}</span>
                      )}
                    </div>
                    <div
                      className={`flex shrink-0 items-center gap-1 rounded-md px-2 py-1 text-xs font-bold ${
                        item.score >= 0.8
                          ? 'border border-green-100 bg-green-50 text-green-700'
                          : item.score >= 0.5
                            ? 'border border-yellow-100 bg-yellow-50 text-yellow-700'
                            : 'border border-gray-100 bg-gray-50 text-gray-600'
                      }`}
                    >
                      <span className="mr-1 text-[10px] text-gray-400 uppercase">Score</span>
                      {typeof item.score === 'number' ? item.score.toFixed(4) : item.score}
                    </div>
                  </div>

                  {item.content && (
                    <div className="border-l-2 border-gray-100 pl-3">
                      <p className="line-clamp-2 text-xs leading-relaxed text-gray-700">
                        {item.content}
                      </p>
                    </div>
                  )}

                  {item.subContent && (
                    <div className="rounded bg-gray-50 p-2.5">
                      <p className="line-clamp-2 font-mono text-[11px] leading-relaxed text-gray-500">
                        {item.subContent}
                      </p>
                    </div>
                  )}

                  <div className="absolute right-5 bottom-5 opacity-0 transition-opacity group-hover:opacity-100">
                    <Maximize2 className="text-primary h-4 w-4" />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 페이지네이션 */}
        {hasSearched && results.length > ITEMS_PER_PAGE && (
          <div className="flex items-center justify-between border-t border-gray-100 bg-white px-6 py-3">
            <span className="text-xs text-gray-500">
              Page <span className="font-bold text-gray-800">{page}</span> of {totalPages}
            </span>
            <div className="flex items-center gap-1">
              <button
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={page === 1}
                className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const start = Math.max(1, Math.min(page - 2, totalPages - 4))
                const pNum = start + i
                if (pNum > totalPages) return null
                return (
                  <button
                    key={pNum}
                    onClick={() => setPage(pNum)}
                    className={`h-8 w-8 rounded-lg text-xs font-bold transition-colors ${
                      page === pNum ? 'bg-primary text-white' : 'text-gray-600 hover:bg-gray-100'
                    }`}
                  >
                    {pNum}
                  </button>
                )
              }).filter(Boolean)}
              <button
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                disabled={page === totalPages}
                className="flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* ── 상세 모달 ── */}
      {isModalOpen && selectedItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
          <div className="animate-in zoom-in-95 flex h-[80vh] w-full max-w-3xl flex-col overflow-hidden rounded-2xl bg-white shadow-2xl duration-200">
            <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
              <div className="flex items-center gap-3">
                <span className="rounded bg-gray-100 px-2 py-0.5 text-xs font-bold text-gray-600">
                  {selectedItem.categoryCode}
                </span>
                <h3 className="line-clamp-1 text-lg font-bold text-gray-800">
                  {selectedItem.title || selectedItem.name}
                </h3>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="rounded-full p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            <div className="scrollbar-thin scrollbar-thumb-gray-300 flex-1 overflow-y-auto p-6">
              <div className="flex flex-col gap-6">
                <div className="rounded-lg border border-gray-100 bg-gray-50 p-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">Score</span>
                      <span className="text-primary text-sm font-bold">
                        {typeof selectedItem.score === 'number'
                          ? selectedItem.score.toFixed(6)
                          : selectedItem.score}
                      </span>
                    </div>
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">Chunk ID</span>
                      <span className="font-mono text-xs text-gray-600">{selectedItem.chunkId}</span>
                    </div>
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">Source</span>
                      <span className="text-xs text-gray-600">{selectedItem.name}</span>
                    </div>
                    {selectedItem.subTitle && (
                      <div className="col-span-2">
                        <span className="mb-1 block text-xs font-bold text-gray-400">Sub Title</span>
                        <span className="text-sm text-gray-700">{selectedItem.subTitle}</span>
                      </div>
                    )}
                    {selectedItem.thirdTitle && (
                      <div className="col-span-2">
                        <span className="mb-1 block text-xs font-bold text-gray-400">Third Title</span>
                        <span className="text-sm text-gray-700">{selectedItem.thirdTitle}</span>
                      </div>
                    )}
                  </div>
                </div>

                <div>
                  <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                    <FileText className="text-primary h-4 w-4" /> Content
                  </h4>
                  <div className="rounded-lg border border-gray-200 bg-white p-5 text-sm leading-8 whitespace-pre-wrap text-gray-800">
                    {selectedItem.content}
                  </div>
                </div>

                {selectedItem.subContent && (
                  <div>
                    <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                      <LayoutTemplate className="text-primary h-4 w-4" /> Sub Content
                    </h4>
                    <div className="rounded-lg border border-gray-200 bg-gray-50/50 p-5 font-mono text-sm leading-7 whitespace-pre-wrap text-gray-600">
                      {selectedItem.subContent}
                    </div>
                  </div>
                )}

                {selectedItem.context && (
                  <div>
                    <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                      <Layers className="text-primary h-4 w-4" /> Context (검색 벡터 원문)
                    </h4>
                    <div className="rounded-lg border border-gray-200 bg-gray-50/50 p-5 text-xs leading-6 whitespace-pre-wrap text-gray-500">
                      {selectedItem.context}
                    </div>
                  </div>
                )}
              </div>
            </div>

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
