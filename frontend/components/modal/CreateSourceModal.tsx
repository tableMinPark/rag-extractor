'use client'

import { useState, useEffect, useRef } from 'react'
import {
  Loader2,
  Upload,
  X,
  FileText,
  CheckCircle2,
  Plus,
  Trash2,
  Settings,
  Database,
  Server,
  Files,
} from 'lucide-react'
import {
  Category,
  PatternType,
  PrefixType,
  RepoResource,
  Source,
} from '@/types/domain'
import {
  createFileSourceApi,
  createRepoSourcesApi,
  getCategoriesSourceApi,
} from '@/api/source'

interface RegisterFormData {
  collectionId: string
  categoryCode: string
  isAuto: boolean
  maxToken: number
  overlapSize: number
  selectType: 'regex' | 'none' | 'token'
  patterns: {
    depth1: PatternType
    depth2: PatternType
    depth3: PatternType
  }
  stopPatterns: string[]
  sourceType: 'file' | 'repo'
  repoHost: string
  repoPort: number
  repoResources: RepoResource[]
}

export const CreateSourceModal = ({
  isOpen,
  onClose,
  onSuccess,
}: {
  isOpen: boolean
  onClose: () => void
  onSuccess: () => void
}) => {
  const DEFAULT_REGISTER_FORM: RegisterFormData = {
    collectionId: '',
    categoryCode: '',
    isAuto: false,
    maxToken: 1200,
    overlapSize: 0,
    selectType: 'regex',
    patterns: {
      depth1: { tokenSize: 0, prefixes: [{ prefix: '', isTitle: true }] },
      depth2: { tokenSize: 0, prefixes: [] },
      depth3: { tokenSize: 0, prefixes: [] },
    },
    stopPatterns: [],
    sourceType: 'file',
    repoHost: '',
    repoPort: 0,
    repoResources: [],
  }

  // --- 상태 관리 ---
  const [formData, setFormData] = useState<RegisterFormData>(
    DEFAULT_REGISTER_FORM,
  )
  const [categories, setCategories] = useState<Category[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])
  const [isUploading, setIsUploading] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const loadData = async () => {
    setIsLoading(true)
    try {
      await getCategoriesSourceApi().then((response) => {
        setCategories(() => response.result)
      })
    } catch (err) {
      console.error(err)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    if (!isOpen) {
      setSelectedFiles([])
      setIsUploading(false)
      setFormData(DEFAULT_REGISTER_FORM)
    } else {
      loadData()
    }
  }, [isOpen])

  // --- 핸들러 ---
  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value, type } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]:
        type === 'checkbox' ? (e.target as HTMLInputElement).checked : value,
    }))
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (files && files.length > 0) {
      const newFiles = Array.from(files)
      setSelectedFiles((prev) => [...prev, ...newFiles])
    }
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const removeFile = (index: number) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index))
  }

  const clearAllFiles = () => {
    setSelectedFiles([])
  }

  // ... (패턴 관련 핸들러들은 기존 코드 유지 - handlePatternChange, addPattern, removePattern 등)
  // 1. 패턴 내용 변경 핸들러
  const handlePatternChange = (
    depth: 'depth1' | 'depth2' | 'depth3',
    index: number,
    field: keyof PrefixType,
    value: any,
  ) => {
    setFormData((prev) => ({
      ...prev,
      patterns: {
        ...prev.patterns,
        [depth]: {
          ...prev.patterns[depth],
          prefixes: prev.patterns[depth].prefixes.map((item, i) =>
            i === index ? { ...item, [field]: value } : item,
          ),
        },
      },
    }))
  }
  const addPattern = (depth: 'depth1' | 'depth2' | 'depth3') => {
    setFormData((prev) => ({
      ...prev,
      patterns: {
        ...prev.patterns,
        [depth]: {
          ...prev.patterns[depth],
          prefixes: [
            ...prev.patterns[depth].prefixes,
            { prefix: '', isTitle: true },
          ],
        },
      },
    }))
  }
  const removePattern = (
    depth: 'depth1' | 'depth2' | 'depth3',
    index: number,
  ) => {
    setFormData((prev) => ({
      ...prev,
      patterns: {
        ...prev.patterns,
        [depth]: {
          ...prev.patterns[depth],
          prefixes: prev.patterns[depth].prefixes.filter((_, i) => i !== index),
        },
      },
    }))
  }
  const addStopPattern = () =>
    setFormData((prev) => ({
      ...prev,
      stopPatterns: [...prev.stopPatterns, ''],
    }))
  const updateStopPattern = (index: number, value: string) => {
    setFormData((prev) => {
      const newStops = [...prev.stopPatterns]
      newStops[index] = value
      return { ...prev, stopPatterns: newStops }
    })
  }
  const removeStopPattern = (index: number) =>
    setFormData((prev) => ({
      ...prev,
      stopPatterns: prev.stopPatterns.filter((_, i) => i !== index),
    }))

  const addRepoResource = () => {
    setFormData((prev) => ({
      ...prev,
      repoResources: [
        ...prev.repoResources,
        { originFileName: '', fileName: '', ext: 'json', path: '', urn: '' },
      ],
    }))
  }
  const updateRepoResource = (
    index: number,
    field: keyof RepoResource,
    value: string,
  ) => {
    setFormData((prev) => {
      const newResources = [...prev.repoResources]
      newResources[index] = { ...newResources[index], [field]: value }
      return { ...prev, repoResources: newResources }
    })
  }
  const removeRepoResource = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      repoResources: prev.repoResources.filter((_, i) => i !== index),
    }))
  }

  // 제출 핸들러
  const handleSubmit = async () => {
    if (!formData.collectionId) return alert('색인 테이블명을 입력해주세요.')
    if (formData.categoryCode === '') return alert('카테고리를 선택해주세요.')
    if (formData.maxToken < 100)
      return alert('토큰 수는 100 이상이여야 합니다.')
    if (formData.sourceType === 'file' && selectedFiles.length === 0)
      return alert('최소 1개 이상의 파일을 업로드해주세요.')

    setIsUploading(true)
    try {
      const patterns = [
        formData.patterns.depth1,
        formData.patterns.depth2,
        formData.patterns.depth3,
      ].filter((pattern) => pattern.prefixes.length > 0)

      const stopPatterns = formData.stopPatterns.filter(
        (stopPattern) => stopPattern.trim() != '',
      )

      if (formData.sourceType === 'file') {
        await createFileSourceApi(
          formData.categoryCode,
          formData.collectionId,
          formData.maxToken,
          formData.overlapSize,
          patterns,
          stopPatterns,
          formData.selectType,
          formData.isAuto,
          selectedFiles,
        ).then((response) => {
          console.log(`📡 ${response.message}`)
          alert('성공적으로 등록되었습니다.')
          onSuccess()
          onClose()
        })
      } else if (formData.sourceType === 'repo') {
        await createRepoSourcesApi(
          formData.categoryCode,
          formData.collectionId,
          formData.maxToken,
          formData.overlapSize,
          patterns,
          stopPatterns,
          formData.selectType,
          formData.isAuto,
          formData.repoHost,
          formData.repoPort,
          formData.repoResources,
        ).then((response) => {
          console.log(`📡 ${response.message}`)
          alert('성공적으로 등록되었습니다.')
          onSuccess()
          onClose()
        })
      }
    } catch (error) {
      console.error(error)
      alert('등록 중 오류가 발생했습니다.')
    } finally {
      setIsUploading(false)
    }
  }

  if (!isOpen) return null

  // --- 공통 스타일 클래스 ---
  const inputClass =
    'w-full rounded-lg border border-gray-200 px-3 py-2 text-sm text-gray-800 outline-none transition-all placeholder:text-gray-400 focus:border-primary focus:ring-1 focus:ring-primary disabled:bg-gray-50'
  const labelClass = 'mb-1.5 block text-xs font-bold text-gray-500'
  const sectionHeaderClass =
    'flex items-center gap-2 border-b border-gray-100 pb-3 text-sm font-bold text-gray-800'

  return (
    <div className="animate-in fade-in fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm duration-200">
      <div className="flex h-[90vh] w-full max-w-4xl flex-col overflow-hidden rounded-2xl bg-white shadow-2xl ring-1 ring-gray-200">
        {/* 헤더 */}
        <div className="flex shrink-0 items-center justify-between border-b border-gray-100 bg-white px-8 py-5">
          <div className="flex items-center gap-3">
            <div className="bg-primary/10 text-primary flex h-10 w-10 items-center justify-center rounded-full">
              <Upload className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-gray-900">문서 등록</h3>
              <p className="text-xs text-gray-500">
                새로운 지식 베이스 문서를 등록하고 설정합니다.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            disabled={isUploading}
            className="rounded-full p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600 disabled:opacity-50"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* 본문 (스크롤) */}
        <div className="scrollbar-thin scrollbar-thumb-gray-200 scrollbar-track-transparent flex-1 overflow-y-auto bg-white p-8">
          <div className="flex flex-col gap-10">
            {/* 1. 기본 정보 섹션 */}
            <section className="flex flex-col gap-5">
              <h4 className={sectionHeaderClass}>
                <Database className="text-primary h-4 w-4" /> 기본 설정 (Basic
                Info)
              </h4>
              <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                <div>
                  <label className={labelClass}>
                    색인 테이블명 (Collection ID)
                  </label>
                  <input
                    type="text"
                    name="collectionId"
                    value={formData.collectionId}
                    onChange={handleInputChange}
                    placeholder="ex: rag_collection_2024"
                    className={inputClass}
                  />
                </div>
                <div>
                  <label className={labelClass}>카테고리 분류</label>
                  <select
                    name="categoryCode"
                    value={formData.categoryCode}
                    onChange={handleInputChange}
                    className={`${inputClass} cursor-pointer bg-white`}
                  >
                    <option key="EMPTY" value="">
                      미선택
                    </option>
                    {categories.map((cat) => (
                      <option key={cat.code} value={cat.code}>
                        {cat.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </section>

            {/* 2. 청킹 설정 섹션 */}
            <section className="flex flex-col gap-5">
              <h4 className={sectionHeaderClass}>
                <Settings className="text-primary h-4 w-4" /> 청킹 설정
                (Chunking Strategy)
              </h4>
              <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
                <div>
                  <label className={labelClass}>청킹 타입</label>
                  <select
                    name="selectType"
                    value={formData.selectType}
                    onChange={handleInputChange}
                    className={`${inputClass} cursor-pointer bg-white`}
                  >
                    <option value="token">Token (글자수 기반)</option>
                    <option value="regex">Regex (정규식 기반)</option>
                    <option value="none">None (지정 안함)</option>
                  </select>
                </div>
                <div>
                  <label className={labelClass}>최대 토큰 수</label>
                  <input
                    type="number"
                    name="maxToken"
                    value={formData.maxToken}
                    onChange={handleInputChange}
                    className={inputClass}
                  />
                </div>
                <div>
                  <label className={labelClass}>오버랩 크기</label>
                  <input
                    type="number"
                    name="overlapSize"
                    value={formData.overlapSize}
                    onChange={handleInputChange}
                    className={inputClass}
                  />
                </div>
              </div>
              {formData.selectType === 'regex' && (
                <div className="mt-2 flex flex-col gap-5 rounded-xl border border-blue-100 bg-blue-50/50 p-5">
                  <div className="flex items-center gap-2 text-xs font-bold text-blue-700">
                    <Settings className="h-3 w-3" /> 정규식 계층 구조 설정 (3
                    Depth)
                  </div>
                  {(['depth1', 'depth2', 'depth3'] as const).map(
                    (depth, idx) => (
                      <div key={depth} className="flex flex-col gap-3">
                        <div className="flex items-center justify-between">
                          <span className="text-[10px] font-bold tracking-wider text-gray-500 uppercase">
                            Level {idx + 1} Patterns
                          </span>
                          <button
                            onClick={() => addPattern(depth)}
                            className="hover:text-primary hover:border-primary/30 flex items-center gap-1 rounded border border-gray-200 bg-white px-2 py-1 text-[10px] font-medium text-gray-600 shadow-sm transition-all hover:bg-gray-50"
                          >
                            <Plus className="h-3 w-3" /> 추가
                          </button>
                        </div>
                        {formData.patterns[depth].prefixes.length === 0 && (
                          <div className="rounded border border-dashed border-gray-300 bg-white/50 p-2 text-center text-[10px] text-gray-400">
                            등록된 패턴이 없습니다.
                          </div>
                        )}
                        <div className="flex flex-col gap-2">
                          {formData.patterns[depth].prefixes.map(
                            (prefixType, pIdx) => (
                              <div
                                key={pIdx}
                                className="animate-in fade-in slide-in-from-top-1 flex items-center gap-2 duration-200"
                              >
                                <div className="relative flex-1">
                                  <span className="absolute top-1/2 left-3 -translate-y-1/2 text-xs font-bold text-gray-400">
                                    /
                                  </span>
                                  <input
                                    type="text"
                                    placeholder="^제\d+조"
                                    value={prefixType.prefix}
                                    onChange={(e) =>
                                      handlePatternChange(
                                        depth,
                                        pIdx,
                                        'prefix',
                                        e.target.value,
                                      )
                                    }
                                    className="focus:border-primary focus:ring-primary w-full rounded-md border border-gray-200 py-1.5 pr-3 pl-6 text-xs outline-none focus:ring-1"
                                  />
                                  <span className="absolute top-1/2 right-3 -translate-y-1/2 text-xs font-bold text-gray-400">
                                    /gm
                                  </span>
                                </div>
                                <label className="flex cursor-pointer items-center gap-1.5 rounded-md border border-gray-200 bg-white px-2 py-1.5 hover:bg-gray-50">
                                  <input
                                    type="checkbox"
                                    checked={prefixType.isTitle}
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
                                    제목 추출
                                  </span>
                                </label>
                                <button
                                  onClick={() => removePattern(depth, pIdx)}
                                  className="flex h-8 w-8 items-center justify-center rounded-md border border-transparent text-gray-400 transition-colors hover:border-red-100 hover:bg-red-50 hover:text-red-500"
                                >
                                  <Trash2 className="h-4 w-4" />
                                </button>
                              </div>
                            ),
                          )}
                        </div>
                      </div>
                    ),
                  )}
                </div>
              )}
              {/* Stop Patterns */}
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
                  {formData.stopPatterns.map((pattern, idx) => (
                    <div key={idx} className="flex items-center gap-2">
                      <input
                        type="text"
                        placeholder="stop regex..."
                        value={pattern}
                        onChange={(e) => updateStopPattern(idx, e.target.value)}
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
                  {formData.stopPatterns.length === 0 && (
                    <div className="rounded border border-dashed border-gray-300 bg-white/50 p-2 text-center text-[10px] text-gray-400">
                      등록된 패턴이 없습니다.
                    </div>
                  )}
                </div>
              </div>
            </section>

            {/* 3. 데이터 소스 설정 섹션 (조건부 렌더링 적용) */}
            <section className="flex flex-col gap-5">
              <h4 className={sectionHeaderClass}>
                <Server className="text-primary h-4 w-4" /> 데이터 소스 (Source
                Type)
              </h4>

              {/* 소스 타입 선택 */}
              <div className="grid grid-cols-2 gap-4">
                <label
                  className={`relative flex cursor-pointer items-center gap-4 rounded-xl border-2 p-4 transition-all ${formData.sourceType === 'file' ? 'border-primary bg-primary/5 ring-primary/20 ring-1' : 'border-gray-100 bg-white hover:border-gray-300 hover:shadow-sm'}`}
                >
                  <input
                    type="radio"
                    name="sourceType"
                    value="file"
                    checked={formData.sourceType === 'file'}
                    onChange={handleInputChange}
                    className="hidden"
                  />
                  <div
                    className={`flex h-10 w-10 items-center justify-center rounded-full ${formData.sourceType === 'file' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-400'}`}
                  >
                    <FileText className="h-5 w-5" />
                  </div>
                  <div>
                    <div className="text-sm font-bold text-gray-900">
                      파일 업로드 (File)
                    </div>
                    <div className="text-xs text-gray-500">
                      로컬 파일을 직접 등록합니다.
                    </div>
                  </div>
                  {formData.sourceType === 'file' && (
                    <CheckCircle2 className="text-primary absolute top-4 right-4 h-5 w-5" />
                  )}
                </label>

                <label
                  className={`relative flex cursor-pointer items-center gap-4 rounded-xl border-2 p-4 transition-all ${formData.sourceType === 'repo' ? 'border-primary bg-primary/5 ring-primary/20 ring-1' : 'border-gray-100 bg-white hover:border-gray-300 hover:shadow-sm'}`}
                >
                  <input
                    type="radio"
                    name="sourceType"
                    value="repo"
                    checked={formData.sourceType === 'repo'}
                    onChange={handleInputChange}
                    className="hidden"
                  />
                  <div
                    className={`flex h-10 w-10 items-center justify-center rounded-full ${formData.sourceType === 'repo' ? 'bg-primary text-white' : 'bg-gray-100 text-gray-400'}`}
                  >
                    <Server className="h-5 w-5" />
                  </div>
                  <div>
                    <div className="text-sm font-bold text-gray-900">
                      저장소 연동 (Repo)
                    </div>
                    <div className="text-xs text-gray-500">
                      원격 저장소의 파일을 가져옵니다.
                    </div>
                  </div>
                  {formData.sourceType === 'repo' && (
                    <CheckCircle2 className="text-primary absolute top-4 right-4 h-5 w-5" />
                  )}
                </label>
              </div>

              {/* [Conditional] FILE Upload */}
              {formData.sourceType === 'file' && (
                <div className="animate-in fade-in slide-in-from-top-2 duration-300">
                  <>
                    <label className="group hover:border-primary mb-4 flex h-36 w-full cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-300 bg-gray-50 transition-all hover:bg-blue-50">
                      <div className="flex flex-col items-center justify-center pt-5 pb-6">
                        <Upload className="group-hover:text-primary mb-3 h-8 w-8 text-gray-400 transition-colors" />
                        <p className="group-hover:text-primary text-sm font-bold text-gray-500">
                          클릭하여 파일 추가 (다중 선택 가능)
                        </p>
                        <p className="mt-1 text-xs text-gray-400">
                          PDF, DOCX, HWP, TXT (Max 10MB)
                        </p>
                      </div>
                      <input
                        type="file"
                        className="hidden"
                        multiple
                        ref={fileInputRef}
                        onChange={handleFileChange}
                      />
                    </label>

                    <div className="flex flex-col gap-2">
                      {selectedFiles.length > 0 && (
                        <div className="mb-2 flex items-center justify-between">
                          <span className="text-xs font-bold text-gray-600">
                            선택된 파일 ({selectedFiles.length})
                          </span>
                          <button
                            onClick={clearAllFiles}
                            className="text-[10px] text-red-500 hover:underline"
                          >
                            전체 삭제
                          </button>
                        </div>
                      )}
                      {selectedFiles.map((file, idx) => (
                        <div
                          key={idx}
                          className="border-primary/20 animate-in fade-in slide-in-from-top-1 flex items-center gap-3 rounded-lg border bg-blue-50 p-3"
                        >
                          <div className="text-primary flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-white shadow-sm">
                            <Files className="h-4 w-4" />
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
                            onClick={() => removeFile(idx)}
                            className="rounded-full p-1.5 text-gray-400 transition-all hover:bg-white hover:text-red-500 hover:shadow-sm"
                          >
                            <X className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      ))}
                    </div>
                  </>
                </div>
              )}

              {/* [Conditional] REPO Settings (기존 유지) */}
              {formData.sourceType === 'repo' && (
                <div className="animate-in fade-in slide-in-from-top-2 flex flex-col gap-6 duration-300">
                  <div className="grid grid-cols-2 gap-6">
                    <div>
                      <label className={labelClass}>Host Address</label>
                      <input
                        type="text"
                        name="repoHost"
                        value={formData.repoHost}
                        onChange={handleInputChange}
                        placeholder="192.168.0.1"
                        className={inputClass}
                      />
                    </div>
                    <div>
                      <label className={labelClass}>Port</label>
                      <input
                        type="number"
                        name="repoPort"
                        value={formData.repoPort}
                        onChange={handleInputChange}
                        placeholder="8080"
                        className={inputClass}
                      />
                    </div>
                  </div>
                  {/* ... Repo Resources ... */}
                  <div className="flex flex-col gap-3">
                    <div className="flex items-center justify-between">
                      <label className={labelClass}>Repository Resources</label>
                      <button
                        onClick={addRepoResource}
                        className="border-primary bg-primary hover:bg-primary-hover flex items-center gap-1 rounded border px-2.5 py-1.5 text-xs font-bold text-white shadow-sm transition-all"
                      >
                        <Plus className="h-3 w-3" /> 리소스 추가
                      </button>
                    </div>
                    {formData.repoResources.length === 0 && (
                      <div className="flex h-20 items-center justify-center rounded-lg border border-dashed border-gray-300 bg-gray-50 text-xs text-gray-400">
                        등록된 리소스가 없습니다. 우측 상단 버튼을 눌러
                        추가해주세요.
                      </div>
                    )}
                    <div className="flex flex-col gap-3">
                      {formData.repoResources.map((res, idx) => (
                        <div
                          key={idx}
                          className="relative grid grid-cols-12 items-end gap-2 rounded-xl border border-gray-200 bg-white p-3 shadow-sm"
                        >
                          <div className="col-span-4 flex flex-col gap-1">
                            <span className="text-[10px] font-bold text-gray-400">
                              Origin Name
                            </span>
                            <input
                              type="text"
                              value={res.originFileName}
                              onChange={(e) =>
                                updateRepoResource(
                                  idx,
                                  'originFileName',
                                  e.target.value,
                                )
                              }
                              className={`${inputClass} px-2 py-1.5 text-xs`}
                              placeholder="원본 파일명"
                            />
                          </div>
                          <div className="col-span-4 flex flex-col gap-1">
                            <span className="text-[10px] font-bold text-gray-400">
                              File Name
                            </span>
                            <input
                              type="text"
                              value={res.fileName}
                              onChange={(e) =>
                                updateRepoResource(
                                  idx,
                                  'fileName',
                                  e.target.value,
                                )
                              }
                              className={`${inputClass} px-2 py-1.5 text-xs`}
                              placeholder="저장 파일명"
                            />
                          </div>
                          <div className="col-span-2 flex flex-col gap-1">
                            <span className="text-[10px] font-bold text-gray-400">
                              Ext
                            </span>
                            <input
                              type="text"
                              value={res.ext}
                              onChange={(e) =>
                                updateRepoResource(idx, 'ext', e.target.value)
                              }
                              className={`${inputClass} px-2 py-1.5 text-xs`}
                              placeholder="json"
                            />
                          </div>
                          <div className="col-span-2 flex justify-end pb-1">
                            <button
                              onClick={() => removeRepoResource(idx)}
                              className="rounded-md bg-red-50 p-1.5 text-red-500 transition-colors hover:bg-red-100"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          </div>
                          <div className="col-span-8 flex flex-col gap-1">
                            <span className="text-[10px] font-bold text-gray-400">
                              Path
                            </span>
                            <input
                              type="text"
                              value={res.path}
                              onChange={(e) =>
                                updateRepoResource(idx, 'path', e.target.value)
                              }
                              className={`${inputClass} px-2 py-1.5 text-xs`}
                              placeholder="/data/extract"
                            />
                          </div>
                          <div className="col-span-4 flex flex-col gap-1">
                            <span className="text-[10px] font-bold text-gray-400">
                              URN
                            </span>
                            <input
                              type="text"
                              value={res.urn}
                              onChange={(e) =>
                                updateRepoResource(idx, 'urn', e.target.value)
                              }
                              className={`${inputClass} px-2 py-1.5 text-xs`}
                              placeholder="urn:code"
                            />
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </section>
          </div>
        </div>

        {/* 하단 버튼 (고정) */}
        <div className="flex shrink-0 items-center justify-end gap-3 border-t border-gray-100 bg-gray-50 px-8 py-5">
          <button
            onClick={onClose}
            disabled={isUploading}
            className="rounded-lg border border-gray-300 bg-white px-5 py-2.5 text-sm font-bold text-gray-600 shadow-sm transition-colors hover:bg-gray-50 disabled:opacity-50"
          >
            취소
          </button>
          <button
            onClick={handleSubmit}
            disabled={isUploading}
            className="bg-primary hover:bg-primary-hover flex items-center gap-2 rounded-lg px-6 py-2.5 text-sm font-bold text-white shadow-md transition-all hover:shadow-lg active:scale-95 disabled:cursor-not-allowed disabled:bg-gray-300"
          >
            {isUploading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                등록 중...
              </>
            ) : (
              '등록하기'
            )}
          </button>
        </div>
      </div>
    </div>
  )
}
