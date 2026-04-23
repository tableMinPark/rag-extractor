'use client'

import React, { useState, useRef } from 'react'
import {
  Upload,
  FileText,
  Files, // 다중 파일 아이콘
  RefreshCw,
  CheckCircle2,
  X,
  Settings,
  Plus,
  Trash2,
  AlertTriangle,
  LayoutTemplate,
  Code,
  Maximize2,
  BookOpen,
  ChevronDown,
  ChevronRight,
} from 'lucide-react'
import { measureRequest } from '@/public/ts/commonUtil'

// ###################################################
// 타입 정의 (Types)
// ###################################################
interface RegexPattern {
  prefix: string
  isTitle: boolean
}

// 개별 청크 정보
interface ChunkResult {
  title: string
  subTitle: string
  thirdTitle: string
  content: string
  subContent: string
}

// [변경] 소스별 청크 그룹 정보
interface SourceGroup {
  sourceName: string // 파일명
  size: number // 파일 크기
  chunks: ChunkResult[]
}

interface ChunkConfig {
  maxToken: number
  overlapSize: number
  chunkingType: 'token' | 'regex' | 'none'
  extractType: 'markdown' | 'html'
  regexPatterns: {
    depth1: RegexPattern[]
    depth2: RegexPattern[]
    depth3: RegexPattern[]
  }
  stopPatterns: string[]
}

export default function ExtractPage() {
  // ###################################################
  // 상태 정의 (State)
  // ###################################################

  // 1. 파일 관련 (다중 파일 배열로 변경)
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])
  const fileInputRef = useRef<HTMLInputElement>(null)

  // 2. 설정 관련
  const [chunkConfig, setChunkConfig] = useState<ChunkConfig>({
    maxToken: 1000,
    overlapSize: 100,
    chunkingType: 'token',
    extractType: 'markdown',
    regexPatterns: {
      depth1: [{ prefix: '^제\\d+장', isTitle: true }],
      depth2: [],
      depth3: [],
    },
    stopPatterns: [],
  })

  // 3. 결과 관련 (소스 그룹 배열로 변경)
  const [isProcessing, setIsProcessing] = useState(false)
  const [extractedResults, setExtractedResults] = useState<SourceGroup[]>([])
  const [processTime, setProcessTime] = useState<string | null>(null)

  // 4. UI 관련 (모달, 접기/펼치기)
  const [detailModalOpen, setDetailModalOpen] = useState(false)
  const [selectedChunk, setSelectedChunk] = useState<ChunkResult | null>(null)
  const [collapsedSources, setCollapsedSources] = useState<
    Record<string, boolean>
  >({})

  // ###################################################
  // 핸들러 (Handler)
  // ###################################################

  // --- 파일 핸들러 ---
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (files && files.length > 0) {
      // 기존 파일에 추가 (중복 제거 로직은 필요시 추가)
      const newFiles = Array.from(files)
      setSelectedFiles((prev) => [...prev, ...newFiles])

      // 결과 초기화
      setExtractedResults([])
      setProcessTime(null)
    }
    // 같은 파일 재선택 가능하게 value 초기화
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const removeFile = (index: number) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index))
    setExtractedResults([]) // 파일 변경 시 결과 초기화
  }

  const clearAllFiles = () => {
    setSelectedFiles([])
    setExtractedResults([])
    setProcessTime(null)
  }

  // --- 설정 변경 핸들러 ---
  const handleConfigChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value } = e.target
    setChunkConfig((prev) => ({ ...prev, [name]: value }))
  }

  const handlePatternChange = (
    depth: 'depth1' | 'depth2' | 'depth3',
    index: number,
    field: keyof RegexPattern,
    value: any,
  ) => {
    setChunkConfig((prev) => {
      const newPatterns = [...prev.regexPatterns[depth]]
      newPatterns[index] = { ...newPatterns[index], [field]: value }
      return {
        ...prev,
        regexPatterns: { ...prev.regexPatterns, [depth]: newPatterns },
      }
    })
  }

  const addPattern = (depth: 'depth1' | 'depth2' | 'depth3') => {
    setChunkConfig((prev) => ({
      ...prev,
      regexPatterns: {
        ...prev.regexPatterns,
        [depth]: [...prev.regexPatterns[depth], { prefix: '', isTitle: false }],
      },
    }))
  }

  const removePattern = (
    depth: 'depth1' | 'depth2' | 'depth3',
    index: number,
  ) => {
    setChunkConfig((prev) => {
      const newPatterns = prev.regexPatterns[depth].filter(
        (_, i) => i !== index,
      )
      return {
        ...prev,
        regexPatterns: { ...prev.regexPatterns, [depth]: newPatterns },
      }
    })
  }

  const addStopPattern = () =>
    setChunkConfig((prev) => ({
      ...prev,
      stopPatterns: [...prev.stopPatterns, ''],
    }))
  const updateStopPattern = (index: number, value: string) => {
    setChunkConfig((prev) => {
      const newStops = [...prev.stopPatterns]
      newStops[index] = value
      return { ...prev, stopPatterns: newStops }
    })
  }
  const removeStopPattern = (index: number) =>
    setChunkConfig((prev) => ({
      ...prev,
      stopPatterns: prev.stopPatterns.filter((_, i) => i !== index),
    }))

  // --- 실행 가능 여부 체크 ---
  const isRunEnabled = () => {
    if (selectedFiles.length === 0 || isProcessing) return false
    if (
      chunkConfig.chunkingType === 'regex' &&
      chunkConfig.regexPatterns.depth1.length === 0
    )
      return false
    if (chunkConfig.chunkingType === 'token' && chunkConfig.maxToken < 100)
      return false
    return true
  }

  // --- 추출 실행 ---
  const handleExtract = async () => {
    if (!isRunEnabled()) return

    setIsProcessing(true)
    setExtractedResults([])
    setCollapsedSources({})

    try {
      await measureRequest(async () => {
        return new Promise((resolve) => setTimeout(resolve, 1000))
      }).then(({ duration }) => {
        // [Mock Data] 각 파일별로 청크 그룹 생성
        const mockGroups: SourceGroup[] = selectedFiles.map((file, fileIdx) => {
          return {
            sourceName: file.name,
            size: file.size,
            // 파일마다 2~5개의 랜덤 청크 생성
            chunks: Array.from({
              length: 2 + Math.floor(Math.random() * 4),
            }).map((_, i) => ({
              title: `${file.name.split('.')[0]} - Section ${i + 1}`,
              subTitle: `Chapter ${i + 1}`,
              thirdTitle: `Paragraph ${i * 10}`,
              content: `Content extracted from ${file.name}.\n(Chunk #${i + 1}) This content is generated based on the ${chunkConfig.extractType} extraction and ${chunkConfig.chunkingType} chunking strategy.`,
              subContent:
                i % 2 === 0
                  ? `Original Page: ${i + 1}\nFile Size: ${(file.size / 1024).toFixed(1)}KB`
                  : '',
            })),
          }
        })

        // [기본값] 모든 그룹 접기
        const initialCollapsedState: Record<string, boolean> = {}
        mockGroups.forEach((g) => {
          initialCollapsedState[g.sourceName] = true
        })

        setCollapsedSources(initialCollapsedState)
        setExtractedResults(mockGroups)
        setProcessTime(`${duration.toFixed(2)}ms`)
      })
    } catch (error) {
      console.error(error)
      alert('오류가 발생했습니다.')
    } finally {
      setIsProcessing(false)
    }
  }

  const openDetailModal = (chunk: ChunkResult) => {
    setSelectedChunk(chunk)
    setDetailModalOpen(true)
  }

  const toggleSourceCollapse = (sourceName: string) => {
    setCollapsedSources((prev) => ({
      ...prev,
      [sourceName]: !prev[sourceName],
    }))
  }

  const totalChunksCount = extractedResults.reduce(
    (acc, curr) => acc + curr.chunks.length,
    0,
  )

  // 스타일 클래스
  const labelClass = 'mb-1.5 block text-xs font-bold text-gray-500'
  const inputClass =
    'w-full rounded-lg border border-gray-200 px-3 py-2 text-sm text-gray-800 outline-none transition-all placeholder:text-gray-400 focus:border-primary focus:ring-1 focus:ring-primary disabled:bg-gray-100 disabled:text-gray-400'

  return (
    <div className="flex h-full w-full flex-col p-6">
      {/* 헤더 */}
      <div className="mb-6 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div>
            <h2 className="flex items-center gap-2 text-2xl font-bold text-gray-800">
              <Files className="text-primary h-6 w-6" />
              파일 문서 청킹
            </h2>
            <p className="mt-1 text-xs text-gray-500">
              다수의 파일을 업로드하여 일괄 청크 생성 테스트를 진행합니다.
            </p>
          </div>
        </div>
      </div>

      <div className="flex min-h-0 flex-1 gap-6">
        {/* ================================================= */}
        {/* [좌측 패널] 설정 및 업로드 */}
        {/* ================================================= */}
        <div className="scrollbar-thin scrollbar-thumb-gray-200 flex w-[450px] flex-col gap-6 overflow-y-auto pr-2">
          {/* 1. 파일 업로드 (다중) */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-sm font-bold text-gray-700">
                대상 파일 업로드
              </h3>
              {selectedFiles.length > 0 && (
                <button
                  onClick={clearAllFiles}
                  className="text-[10px] text-red-500 hover:underline"
                >
                  전체 삭제
                </button>
              )}
            </div>

            {/* 드래그 앤 드롭 영역 */}
            <label className="group hover:border-primary mb-4 flex h-32 w-full cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-gray-300 bg-gray-50 transition-colors hover:bg-blue-50">
              <div className="flex flex-col items-center justify-center pt-3 pb-4">
                <Upload className="group-hover:text-primary mb-2 h-6 w-6 text-gray-400 transition-colors" />
                <p className="mb-1 text-sm font-bold text-gray-500 group-hover:text-gray-700">
                  클릭하여 파일 추가 (다중 선택 가능)
                </p>
                <p className="text-[10px] text-gray-400">
                  PDF, HWP, DOCX, TXT (MAX 10MB)
                </p>
              </div>
              <input
                type="file"
                className="hidden"
                multiple // 다중 파일 선택 활성화
                ref={fileInputRef}
                onChange={handleFileUpload}
                accept=".pdf,.hwp,.hwpx,.docx,.txt"
              />
            </label>

            {/* 선택된 파일 목록 */}
            <div className="flex flex-col gap-2">
              {selectedFiles.length === 0 && (
                <div className="py-2 text-center text-xs text-gray-400">
                  선택된 파일이 없습니다.
                </div>
              )}
              {selectedFiles.map((file, index) => (
                <div
                  key={index}
                  className="border-primary/20 animate-in fade-in slide-in-from-top-1 relative flex items-center gap-3 rounded-lg border bg-blue-50 p-3 duration-200"
                >
                  <div className="text-primary flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-white shadow-sm">
                    <FileText className="h-4 w-4" />
                  </div>
                  <div className="flex-1 overflow-hidden">
                    <p className="truncate text-xs font-bold text-gray-800">
                      {file.name}
                    </p>
                    <p className="text-[10px] text-gray-500">
                      {(file.size / 1024).toFixed(1)} KB
                    </p>
                  </div>
                  <button
                    onClick={() => removeFile(index)}
                    className="rounded-full p-1.5 text-gray-400 transition-all hover:bg-white hover:text-red-500 hover:shadow-sm"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* 2. 청킹 설정 (이전과 동일) */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <h3 className="mb-4 flex items-center gap-2 text-sm font-bold text-gray-700">
              <Settings className="text-primary h-4 w-4" /> 청킹 전략 설정
            </h3>
            <div className="flex flex-col gap-5">
              {/* 추출 타입 */}
              <div>
                <label className={labelClass}>추출 포맷</label>
                <div className="grid grid-cols-2 gap-2">
                  {(['markdown', 'html'] as const).map((type) => (
                    <label
                      key={type}
                      className={`flex cursor-pointer items-center justify-center gap-2 rounded-lg border px-3 py-2 transition-all ${chunkConfig.extractType === type ? 'border-primary text-primary ring-primary bg-blue-50 ring-1' : 'border-gray-200 text-gray-600 hover:bg-gray-50'}`}
                    >
                      <input
                        type="radio"
                        name="extractType"
                        value={type}
                        checked={chunkConfig.extractType === type}
                        onChange={handleConfigChange}
                        className="hidden"
                      />
                      {type === 'markdown' ? (
                        <LayoutTemplate className="h-4 w-4" />
                      ) : (
                        <Code className="h-4 w-4" />
                      )}
                      <span className="text-xs font-bold uppercase">
                        {type}
                      </span>
                    </label>
                  ))}
                </div>
              </div>

              {/* 청킹 방식 */}
              <div>
                <label className={labelClass}>청킹 방식</label>
                <select
                  name="chunkingType"
                  value={chunkConfig.chunkingType}
                  onChange={handleConfigChange}
                  className={`${inputClass} cursor-pointer bg-white`}
                >
                  <option value="token">Token (글자수 기반)</option>
                  <option value="regex">Regex (정규식 기반)</option>
                  <option value="none">None (지정 안함)</option>
                </select>
              </div>

              {/* 토큰 설정 */}
              {chunkConfig.chunkingType === 'token' && (
                <div className="animate-in fade-in zoom-in-95 grid grid-cols-2 gap-4 duration-200">
                  <div>
                    <label className={labelClass}>최대 토큰 (Min 100)</label>
                    <input
                      type="number"
                      name="maxToken"
                      min={100}
                      value={chunkConfig.maxToken}
                      onChange={handleConfigChange}
                      className={`${inputClass} ${chunkConfig.maxToken < 100 ? 'border-red-300 ring-1 ring-red-200' : ''}`}
                    />
                  </div>
                  <div>
                    <label className={labelClass}>오버랩 크기</label>
                    <input
                      type="number"
                      name="overlapSize"
                      value={chunkConfig.overlapSize}
                      onChange={handleConfigChange}
                      className={inputClass}
                    />
                  </div>
                </div>
              )}

              {/* 정규식 설정 */}
              {chunkConfig.chunkingType === 'regex' && (
                <div className="animate-in fade-in zoom-in-95 flex flex-col gap-6 duration-200">
                  <div className="rounded-xl border border-blue-100 bg-blue-50/50 p-4">
                    <div className="mb-3 flex items-center justify-between">
                      <div className="flex items-center gap-2 text-xs font-bold text-blue-700">
                        <Settings className="h-3 w-3" /> 계층 구조
                      </div>
                      {chunkConfig.regexPatterns.depth1.length === 0 && (
                        <span className="flex items-center gap-1 text-[10px] font-bold text-red-500">
                          <AlertTriangle className="h-3 w-3" /> 1 Depth 필수
                        </span>
                      )}
                    </div>
                    <div className="flex flex-col gap-4">
                      {(['depth1', 'depth2', 'depth3'] as const).map(
                        (depth, idx) => (
                          <div key={depth} className="flex flex-col gap-2">
                            <div className="flex items-center justify-between">
                              <span className="text-[10px] font-bold tracking-wider text-gray-500 uppercase">
                                Level {idx + 1}
                              </span>
                              <button
                                onClick={() => addPattern(depth)}
                                className="hover:text-primary hover:border-primary/30 flex items-center gap-1 rounded border border-gray-200 bg-white px-2 py-1 text-[10px] font-medium text-gray-600 shadow-sm transition-all hover:bg-gray-50"
                              >
                                <Plus className="h-3 w-3" /> 추가
                              </button>
                            </div>
                            {chunkConfig.regexPatterns[depth].length === 0 && (
                              <div className="rounded border border-dashed border-gray-300 bg-white/50 p-2 text-center text-[10px] text-gray-400">
                                {idx === 0 ? '필수 입력' : '패턴 없음'}
                              </div>
                            )}
                            <div className="flex flex-col gap-2">
                              {chunkConfig.regexPatterns[depth].map(
                                (pattern, pIdx) => (
                                  <div
                                    key={pIdx}
                                    className="flex items-center gap-2"
                                  >
                                    <input
                                      type="text"
                                      placeholder="regex..."
                                      value={pattern.prefix}
                                      onChange={(e) =>
                                        handlePatternChange(
                                          depth,
                                          pIdx,
                                          'prefix',
                                          e.target.value,
                                        )
                                      }
                                      className="focus:border-primary focus:ring-primary w-full rounded-md border border-gray-200 px-3 py-1.5 text-xs outline-none focus:ring-1"
                                    />
                                    <label className="flex shrink-0 cursor-pointer items-center gap-1 rounded-md border border-gray-200 bg-white px-1.5 py-1.5 hover:bg-gray-50">
                                      <input
                                        type="checkbox"
                                        checked={pattern.isTitle}
                                        onChange={(e) =>
                                          handlePatternChange(
                                            depth,
                                            pIdx,
                                            'isTitle',
                                            e.target.checked,
                                          )
                                        }
                                        className="text-primary focus:ring-primary h-3 w-3 rounded border-gray-300"
                                      />
                                      <span className="text-[10px] font-medium text-gray-600">
                                        제목
                                      </span>
                                    </label>
                                    <button
                                      onClick={() => removePattern(depth, pIdx)}
                                      className="flex h-7 w-7 shrink-0 items-center justify-center rounded-md border border-transparent text-gray-400 transition-colors hover:border-red-100 hover:bg-red-50 hover:text-red-500"
                                    >
                                      <Trash2 className="h-3.5 w-3.5" />
                                    </button>
                                  </div>
                                ),
                              )}
                            </div>
                          </div>
                        ),
                      )}
                    </div>
                  </div>
                  <div className="rounded-xl border border-red-100 bg-red-50/50 p-4">
                    <div className="mb-3 flex items-center justify-between">
                      <div className="flex items-center gap-2 text-xs font-bold text-red-700">
                        <X className="h-3 w-3" /> 중단 패턴
                      </div>
                      <button
                        onClick={addStopPattern}
                        className="flex items-center gap-1 rounded border border-gray-200 bg-white px-2 py-1 text-[10px] font-medium text-gray-600 shadow-sm transition-all hover:border-red-200 hover:bg-red-50 hover:text-red-600"
                      >
                        <Plus className="h-3 w-3" /> 추가
                      </button>
                    </div>
                    <div className="flex flex-col gap-2">
                      {chunkConfig.stopPatterns.map((pattern, idx) => (
                        <div key={idx} className="flex items-center gap-2">
                          <input
                            type="text"
                            placeholder="stop regex..."
                            value={pattern}
                            onChange={(e) =>
                              updateStopPattern(idx, e.target.value)
                            }
                            className="w-full rounded-md border border-gray-200 px-3 py-1.5 text-xs outline-none focus:border-red-400 focus:ring-1 focus:ring-red-400"
                          />
                          <button
                            onClick={() => removeStopPattern(idx)}
                            className="flex h-7 w-7 shrink-0 items-center justify-center rounded-md border border-transparent text-gray-400 transition-colors hover:border-red-100 hover:bg-red-50 hover:text-red-500"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

          <button
            onClick={handleExtract}
            disabled={!isRunEnabled()}
            className="bg-primary hover:bg-primary-hover flex w-full items-center justify-center gap-2 rounded-xl py-4 text-base font-bold text-white shadow-md transition-all active:scale-95 disabled:cursor-not-allowed disabled:bg-gray-300 disabled:shadow-none"
          >
            {isProcessing ? (
              <>
                <RefreshCw className="h-5 w-5 animate-spin" /> 처리 중...
              </>
            ) : (
              <>
                <CheckCircle2 className="h-5 w-5" /> 데이터 호출 및 청킹 시작
              </>
            )}
          </button>
        </div>

        {/* ================================================= */}
        {/* [우측 패널] 결과 출력 (파일별 그룹핑) */}
        {/* ================================================= */}
        <div className="flex flex-1 flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
          {/* 헤더 */}
          <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-6 py-4">
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold text-gray-700">
                추출 결과 (Chunks)
              </span>
              {extractedResults.length > 0 && (
                <span className="rounded-full bg-blue-100 px-2 py-0.5 text-xs font-bold text-blue-700">
                  Total {totalChunksCount}
                </span>
              )}
            </div>
            {processTime && (
              <span className="font-mono text-xs text-gray-400">
                Time: {processTime}
              </span>
            )}
          </div>

          {/* 결과 리스트 */}
          <div className="scrollbar-thin scrollbar-thumb-gray-200 flex-1 overflow-y-auto bg-gray-50/30 p-6">
            {extractedResults.length > 0 ? (
              <div className="flex flex-col gap-8">
                {extractedResults.map((group, groupIdx) => (
                  <div key={groupIdx} className="flex flex-col gap-3">
                    {/* 소스 헤더 (파일명) */}
                    <div
                      className="hover:border-primary/50 flex cursor-pointer items-center justify-between rounded-lg border border-gray-200 bg-white px-4 py-3 shadow-sm transition-colors"
                      onClick={() => toggleSourceCollapse(group.sourceName)}
                    >
                      <div className="flex items-center gap-3 overflow-hidden">
                        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-blue-50 text-blue-600">
                          <FileText className="h-4 w-4" />
                        </div>
                        <div className="flex flex-col overflow-hidden">
                          <span className="truncate font-mono text-xs font-bold text-gray-800">
                            {group.sourceName}
                          </span>
                          <span className="text-[10px] text-gray-500">
                            {(group.size / 1024).toFixed(1)} KB •{' '}
                            {group.chunks.length} chunks
                          </span>
                        </div>
                      </div>
                      <div className="text-gray-400">
                        {collapsedSources[group.sourceName] ? (
                          <ChevronRight className="h-5 w-5" />
                        ) : (
                          <ChevronDown className="h-5 w-5" />
                        )}
                      </div>
                    </div>

                    {/* 해당 파일의 청크 그리드 (접히지 않았을 때만 표시) */}
                    {!collapsedSources[group.sourceName] && (
                      <div className="animate-in fade-in slide-in-from-top-1 grid grid-cols-1 gap-4 pl-2 duration-200 xl:grid-cols-2">
                        {group.chunks.map((chunk, index) => (
                          <div
                            key={index}
                            onClick={() => openDetailModal(chunk)}
                            className="group hover:border-primary/50 relative flex cursor-pointer flex-col gap-3 rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-all hover:shadow-md"
                          >
                            <div className="flex flex-col gap-1 border-b border-gray-50 pb-2">
                              <div className="flex items-center gap-2">
                                <span className="rounded bg-gray-100 px-1.5 py-0.5 text-[10px] font-bold text-gray-500">
                                  #{index + 1}
                                </span>
                                {chunk.title && (
                                  <h4 className="line-clamp-1 text-sm font-bold text-gray-900">
                                    {chunk.title}
                                  </h4>
                                )}
                              </div>
                              {(chunk.subTitle || chunk.thirdTitle) && (
                                <div className="flex items-center gap-1.5 text-xs text-gray-500">
                                  {chunk.subTitle && (
                                    <span>{chunk.subTitle}</span>
                                  )}
                                  {chunk.thirdTitle && (
                                    <>
                                      <span className="text-gray-300">/</span>
                                      <span>{chunk.thirdTitle}</span>
                                    </>
                                  )}
                                </div>
                              )}
                            </div>
                            <div>
                              <span className="mb-1 block text-[10px] font-bold text-gray-400 uppercase">
                                Content
                              </span>
                              <p className="line-clamp-2 text-xs leading-relaxed text-gray-700">
                                {chunk.content}
                              </p>
                            </div>
                            {chunk.subContent && (
                              <div className="rounded bg-gray-50 p-2">
                                <span className="mb-1 block text-[10px] font-bold text-gray-400 uppercase">
                                  Sub Content
                                </span>
                                <p className="line-clamp-2 text-xs leading-relaxed text-gray-600">
                                  {chunk.subContent}
                                </p>
                              </div>
                            )}
                            <div className="absolute top-4 right-4 opacity-0 transition-opacity group-hover:opacity-100">
                              <Maximize2 className="text-primary h-4 w-4" />
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex h-full flex-col items-center justify-center gap-4 text-gray-400">
                <div className="rounded-full bg-gray-100 p-6">
                  <BookOpen className="h-10 w-10 text-gray-300" />
                </div>
                <div className="text-center">
                  <p className="text-sm font-bold text-gray-500">
                    결과 데이터가 없습니다.
                  </p>
                  <p className="mt-1 text-xs">
                    파일을 업로드하고 청킹을 시작해보세요.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 모달 (이전과 동일) */}
      {detailModalOpen && selectedChunk && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
          <div className="animate-in zoom-in-95 flex h-[80vh] w-full max-w-3xl flex-col overflow-hidden rounded-2xl bg-white shadow-2xl duration-200">
            <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
              <h3 className="text-lg font-bold text-gray-800">
                청크 상세 정보
              </h3>
              <button
                onClick={() => setDetailModalOpen(false)}
                className="rounded-full p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>
            <div className="scrollbar-thin scrollbar-thumb-gray-300 flex-1 overflow-y-auto p-6">
              <div className="flex flex-col gap-6">
                <div className="rounded-lg border border-gray-100 bg-gray-50 p-4">
                  <div className="flex flex-col gap-4">
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">
                        Title
                      </span>
                      <p className="text-sm font-bold text-gray-900">
                        {selectedChunk.title || '-'}
                      </p>
                    </div>
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">
                        Sub Title
                      </span>
                      <p className="text-sm text-gray-700">
                        {selectedChunk.subTitle || '-'}
                      </p>
                    </div>
                    <div>
                      <span className="mb-1 block text-xs font-bold text-gray-400">
                        Third Title
                      </span>
                      <p className="text-sm text-gray-700">
                        {selectedChunk.thirdTitle || '-'}
                      </p>
                    </div>
                  </div>
                </div>
                <div>
                  <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                    <FileText className="text-primary h-4 w-4" /> Main Content
                  </h4>
                  <div className="rounded-lg border border-gray-200 bg-white p-4 font-mono text-sm leading-7 whitespace-pre-wrap text-gray-700">
                    {selectedChunk.content}
                  </div>
                </div>
                {selectedChunk.subContent && (
                  <div>
                    <h4 className="mb-2 flex items-center gap-2 border-b border-gray-100 pb-2 text-sm font-bold text-gray-800">
                      <LayoutTemplate className="text-primary h-4 w-4" /> Sub
                      Content
                    </h4>
                    <div className="rounded-lg border border-gray-200 bg-gray-50/50 p-4 font-mono text-sm leading-7 whitespace-pre-wrap text-gray-600">
                      {selectedChunk.subContent}
                    </div>
                  </div>
                )}
              </div>
            </div>
            <div className="border-t border-gray-100 bg-gray-50 px-6 py-4 text-right">
              <button
                onClick={() => setDetailModalOpen(false)}
                className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-bold text-gray-700 shadow-sm hover:bg-gray-50"
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
