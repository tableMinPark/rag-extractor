'use client'

import { useState, useEffect, useRef } from 'react'
import {
  CheckCircle2,
  Database,
  Files,
  FileText,
  Loader2,
  Plus,
  Server,
  Trash,
  Trash2,
  Upload,
  X,
} from 'lucide-react'
import { Category, Collection, RepoResource } from '@/types/domain'
import {
  getCategoriesSourceApi,
  createFileSourceApi,
  createRepoSourcesApi,
} from '@/api/source'

interface FormData {
  collectionId: string
  categoryCode: string
  isAuto: boolean
  sourceType: string
  files: File[]
  repoResources: RepoResource[]
}

const DEFAULT_FORM_DATA: FormData = {
  collectionId: '',
  categoryCode: '',
  isAuto: false,
  sourceType: 'file',
  files: [],
  repoResources: [],
}

export const ModalCreateSource = ({ onClose }: { onClose: () => void }) => {
  // ###################################################
  // 상태 관리
  // ###################################################
  const [isLoading, setIsLoading] = useState(false)
  const [collections, setCollections] = useState<Collection[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [formData, setFormData] = useState<FormData>(DEFAULT_FORM_DATA)

  // ###################################################
  // 랜더링 이펙트
  // ###################################################
  useEffect(() => {
    handleGetCollections()
    handleGetCategories()
  }, [])

  // ###################################################
  // 핸들러
  // ###################################################
  const handleGetCollections = async () => {
    // 컬렉션은 별도 API가 없어 하드코딩
    setCollections([
      { collectionId: 'NHIS_AI', collectionName: 'AI' },
    ])
  }

  const handleGetCategories = async () => {
    try {
      const response = await getCategoriesSourceApi()
      setCategories(response.result)
    } catch (e) {
      console.error('카테고리 조회 실패', e)
    }
  }

  const handleCreateSource = async () => {
    if (!formData.categoryCode) return alert('카테고리를 선택해주세요.')
    if (!formData.collectionId) return alert('색인 테이블을 선택해주세요.')
    if (formData.sourceType === 'file' && formData.files.length === 0)
      return alert('파일을 선택해주세요.')
    if (
      formData.sourceType === 'repo' &&
      formData.repoResources.length === 0
    )
      return alert('리소스를 추가해주세요.')

    setIsLoading(true)
    try {
      if (formData.sourceType === 'file') {
        await createFileSourceApi(
          formData.categoryCode,
          formData.collectionId,
          1200,
          0,
          [],
          [],
          'SELECT-TYPE-EMPTY',
          formData.isAuto,
          formData.files,
        )
      } else {
        await createRepoSourcesApi(
          formData.categoryCode,
          formData.collectionId,
          1200,
          0,
          [],
          [],
          'SELECT-TYPE-EMPTY',
          formData.isAuto,
          '',
          0,
          formData.repoResources,
        )
      }
      onClose()
    } catch (e) {
      console.error('문서 등록 실패', e)
      alert('문서 등록에 실패했습니다.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleChangeIsAuto = (isAuto: boolean) => {
    setFormData((prev) => ({
      ...prev,
      isAuto,
    }))
  }

  const handleChangeCollectionId = (collectionId: string) => {
    setFormData((prev) => ({
      ...prev,
      collectionId,
    }))
  }

  const handleChangeCategoryCode = (categoryCode: string) => {
    setFormData((prev) => ({
      ...prev,
      categoryCode,
    }))
  }

  const handleChangeSourceType = (sourceType: string) => {
    setFormData((prev) => ({
      ...prev,
      sourceType,
    }))
  }

  const handleChangeFiles = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const files = Array.from(e.target.files)
      setFormData((prev) => ({
        ...prev,
        files: [...prev.files, ...files],
      }))
    }
  }

  const handleRemoveFiles = () => {
    setFormData((prev) => ({
      ...prev,
      files: [],
    }))
  }

  const handleRemoveFile = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      files: [...prev.files.filter((_, i) => i !== index)],
    }))
  }

  const handleAddRepoResource = () => {
    setFormData((prev) => ({
      ...prev,
      repoResources: [
        ...prev.repoResources,
        {
          name: '',
          targetUrl: '',
          originFileName: '',
          fileName: '',
          ext: '',
          path: '',
          urn: '',
        },
      ],
    }))
  }

  const handleChangeRepoResourceName = (index: number, name: string) => {
    setFormData((prev) => ({
      ...prev,
      repoResources: [
        ...prev.repoResources.map((repoResource, i) => {
          if (i === index) {
            return {
              ...repoResource,
              name: name,
            }
          }
          return repoResource
        }),
      ],
    }))
  }

  const handleChangeRepoResourceTargetUrl = (
    index: number,
    targetUrl: string,
  ) => {
    setFormData((prev) => ({
      ...prev,
      repoResources: [
        ...prev.repoResources.map((repoResource, i) => {
          if (i === index) {
            return {
              ...repoResource,
              targetUrl: targetUrl,
            }
          }
          return repoResource
        }),
      ],
    }))
  }

  const handleRemoveRepoResource = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      repoResources: [...prev.repoResources.filter((_, i) => i !== index)],
    }))
  }

  // ###################################################
  // 렌더링 (Render)
  // ###################################################
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
              <p className="text-xs text-gray-500">새로운 문서를 등록합니다.</p>
            </div>
          </div>
          <button
            onClick={onClose}
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
                <Database className="text-primary h-4 w-4" /> 기본 설정
              </h4>
              <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
                <div>
                  <label className={labelClass}>
                    카테고리 <span className="text-red-500">*</span>
                  </label>
                  <select
                    name="categoryCode"
                    value={formData.categoryCode}
                    onChange={(e) => handleChangeCategoryCode(e.target.value)}
                    className={`${inputClass} cursor-pointer bg-white`}
                  >
                    <option key="" value="">
                      미선택
                    </option>
                    {categories.map((cat) => (
                      <option key={cat.code} value={cat.code}>
                        {cat.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className={labelClass}>
                    색인 테이블 <span className="text-red-500">*</span>
                  </label>
                  <select
                    name="collectionId"
                    value={formData.collectionId}
                    onChange={(e) => handleChangeCollectionId(e.target.value)}
                    className={`${inputClass} cursor-pointer bg-white`}
                  >
                    <option key="" value="">
                      미선택
                    </option>
                    {collections.map((collection) => (
                      <option
                        key={collection.collectionId}
                        value={collection.collectionId}
                      >
                        {collection.collectionName}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className={labelClass}>
                    자동화여부 <span className="text-red-500">*</span>
                  </label>
                  <select
                    name="isAuto"
                    value={formData.isAuto ? 'Y' : 'N'}
                    onChange={(e) => handleChangeIsAuto(e.target.value == 'Y')}
                    className={`${inputClass} cursor-pointer bg-white`}
                  >
                    <option key="N" value="N">
                      수동
                    </option>
                    <option key="Y" value="Y">
                      자동
                    </option>
                  </select>
                </div>
              </div>
            </section>
            {/* 2. 데이터 소스 설정 섹션 (조건부 렌더링 적용) */}
            <section className="flex flex-col gap-5">
              <h4 className={sectionHeaderClass}>
                <Server className="text-primary h-4 w-4" /> 데이터 소스
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
                    onChange={(e) => handleChangeSourceType(e.target.value)}
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
                    onChange={(e) => handleChangeSourceType(e.target.value)}
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
              {formData.sourceType === 'file' && (
                <div className="animate-in fade-in slide-in-from-top-2 duration-300">
                  <label className="group hover:border-primary mb-4 flex h-36 w-full cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-300 bg-gray-50 transition-all hover:bg-blue-50">
                    <div className="flex flex-col items-center justify-center pt-5 pb-6">
                      <Upload className="group-hover:text-primary mb-3 h-8 w-8 text-gray-400 transition-colors" />
                      <p className="group-hover:text-primary text-sm font-bold text-gray-500">
                        클릭하여 파일 추가 (다중 선택 가능)
                      </p>
                      <p className="mt-1 text-xs text-gray-400">
                        PDF, HWP, HWPX, TXT (Max 10MB)
                      </p>
                    </div>
                    <input
                      type="file"
                      className="hidden"
                      multiple
                      onChange={handleChangeFiles}
                    />
                  </label>
                  <div className="flex flex-col gap-2">
                    {formData.files.length > 0 && (
                      <div className="mb-2 flex items-end justify-between">
                        <span className="text-xs font-bold text-gray-600">
                          선택된 파일 ({formData.files.length})
                        </span>
                        <button
                          onClick={handleRemoveFiles}
                          className="border-primary bg-primary hover:bg-primary-hover flex items-center gap-2 rounded-lg border px-2.5 py-1.5 text-xs font-bold text-white shadow-sm transition-all"
                        >
                          <Trash className="h-3 w-3" />
                          전체 삭제
                        </button>
                      </div>
                    )}
                    {formData.files.map((file, index) => (
                      <div
                        key={index}
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
                          onClick={() => handleRemoveFile(index)}
                          className="rounded-full p-1.5 text-gray-400 transition-all hover:bg-white hover:text-red-500 hover:shadow-sm"
                        >
                          <X className="h-3.5 w-3.5" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
              {formData.sourceType === 'repo' && (
                <div className="animate-in fade-in slide-in-from-top-2 flex flex-col gap-6 duration-300">
                  <div className="flex flex-col gap-3">
                    <div className="flex items-end justify-between">
                      <span className="text-xs font-bold text-gray-600">
                        등록된 리소스 ({formData.repoResources.length})
                      </span>
                      <button
                        onClick={handleAddRepoResource}
                        className="border-primary bg-primary hover:bg-primary-hover flex items-center gap-2 rounded-lg border px-2.5 py-1.5 text-xs font-bold text-white shadow-sm transition-all"
                      >
                        <Plus className="h-3 w-3" />
                        리소스 추가
                      </button>
                    </div>
                    {formData.repoResources.length === 0 && (
                      <div className="flex h-20 items-center justify-center rounded-lg border border-dashed border-gray-300 bg-gray-50 text-sm text-gray-400">
                        등록된 리소스가 없습니다. 우측 상단 버튼을 눌러
                        추가해주세요.
                      </div>
                    )}
                    <div className="flex w-full flex-col gap-3">
                      {formData.repoResources.map((repoResource, index) => (
                        <div
                          key={index}
                          className="flex flex-col gap-2 rounded-xl border border-gray-200 bg-white p-4 shadow-sm"
                        >
                          <div className="flex w-full items-end justify-between gap-4">
                            <div className="flex w-full flex-col gap-2">
                              <span className="text-[10px] font-bold text-gray-400">
                                문서명 <span className="text-red-500">*</span>
                              </span>
                              <input
                                type="text"
                                value={repoResource.name}
                                onChange={(e) =>
                                  handleChangeRepoResourceName(
                                    index,
                                    e.target.value,
                                  )
                                }
                                className={`${inputClass} px-2 py-1.5 text-xs`}
                                placeholder="문서명을 입력해주세요."
                              />
                            </div>
                            <div className="flex h-8 w-8 flex-col gap-2">
                              <button
                                onClick={() => handleRemoveRepoResource(index)}
                                className="rounded-md bg-red-50 px-2 py-1.5 text-red-500 transition-colors hover:bg-red-100"
                              >
                                <Trash2 className="h-4 w-4" />
                              </button>
                            </div>
                          </div>
                          <div className="flex w-full items-end justify-between">
                            <div className="flex w-full flex-col gap-2">
                              <span className="text-[10px] font-bold text-gray-400">
                                URL <span className="text-red-500">*</span>
                              </span>
                              <input
                                type="text"
                                value={repoResource.targetUrl}
                                onChange={(e) =>
                                  handleChangeRepoResourceTargetUrl(
                                    index,
                                    e.target.value,
                                  )
                                }
                                className={`${inputClass} px-2 py-1.5 text-xs`}
                                placeholder="192.168.0.1:8080/data/json"
                              />
                            </div>
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
            disabled={isLoading}
            className="rounded-lg border border-gray-300 bg-white px-5 py-2.5 text-sm font-bold text-gray-600 shadow-sm transition-colors hover:bg-gray-50 disabled:opacity-50"
          >
            취소
          </button>
          <button
            onClick={handleCreateSource}
            disabled={isLoading}
            className="bg-primary hover:bg-primary-hover flex items-center gap-2 rounded-lg px-6 py-2.5 text-sm font-bold text-white shadow-md transition-all hover:shadow-lg active:scale-95 disabled:cursor-not-allowed disabled:bg-gray-300"
          >
            {isLoading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                등록중...
              </>
            ) : (
              '등록'
            )}
          </button>
        </div>
      </div>
    </div>
  )
}
