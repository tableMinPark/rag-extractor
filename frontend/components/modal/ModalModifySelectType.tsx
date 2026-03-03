'use client'

import { useState, useEffect } from 'react'
import { Loader2, Upload, X, Plus, Trash2, Settings } from 'lucide-react'
import { PatternType, PrefixType } from '@/types/domain'

interface FormData {
  selectType: string
  maxToken: number
  overlapSize: number
  patterns: PatternType[]
  stopPatterns: string[]
}

const DEFAULT_FORM_DATA: FormData = {
  selectType: 'SELECT-TYPE-EMPTY',
  maxToken: 1200,
  overlapSize: 0,
  patterns: [
    { tokenSize: 0, prefixes: [{ prefix: '', isTitle: true }] },
    { tokenSize: 0, prefixes: [] },
    { tokenSize: 0, prefixes: [] },
  ],
  stopPatterns: [],
}

export const ModalModifySelectType = ({ onClose }: { onClose: () => void }) => {
  // ###################################################
  // 상태 관리
  // ###################################################
  const [isLoading, setIsLoading] = useState(false)
  const [formData, setFormData] = useState<FormData>(DEFAULT_FORM_DATA)

  // ###################################################
  // 랜더링 이펙트
  // ###################################################
  useEffect(() => {}, [])

  // ###################################################
  // 핸들러
  // ###################################################
  const handleChangeSelectType = (selectType: string) => {
    setFormData((prev) => ({
      ...prev,
      selectType,
    }))
  }

  const handleChangeMaxToken = (maxToken: number) => {
    setFormData((prev) => ({
      ...prev,
      maxToken,
    }))
  }

  const handleChangeOverlap = (overlapSize: number) => {
    setFormData((prev) => ({
      ...prev,
      overlapSize,
    }))
  }

  const handleAddPattern = (depth: number) => {
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

  const handleChangePrefix = (depth: number, index: number, prefix: string) => {
    setFormData((prev) => ({
      ...prev,
      patterns: {
        ...prev.patterns,
        [depth]: {
          ...prev.patterns[depth],
          prefixes: prev.patterns[depth].prefixes.map((item, i) =>
            i === index ? { ...item, prefix } : item,
          ),
        },
      },
    }))
  }

  const handleChangeIsTitle = (
    depth: number,
    index: number,
    isTitle: boolean,
  ) => {
    setFormData((prev) => ({
      ...prev,
      patterns: {
        ...prev.patterns,
        [depth]: {
          ...prev.patterns[depth],
          prefixes: prev.patterns[depth].prefixes.map((item, i) =>
            i === index ? { ...item, isTitle } : item,
          ),
        },
      },
    }))
  }

  const handleRemovePattern = (depth: number, index: number) => {
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

  const handleAddStopPattern = () => {
    setFormData((prev) => ({
      ...prev,
      stopPatterns: [...prev.stopPatterns, ''],
    }))
  }

  const handleChangeStopPattern = (index: number, stopPattern: string) => {
    setFormData((prev) => {
      const newStopPatterns = [...prev.stopPatterns]
      newStopPatterns[index] = stopPattern
      return { ...prev, stopPatterns: newStopPatterns }
    })
  }

  const handleRemoveStopPattern = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      stopPatterns: prev.stopPatterns.filter((_, i) => i !== index),
    }))
  }

  const handleModify = async () => {
    console.log(formData)
  }

  // --- 공통 스타일 클래스 ---
  const inputClass =
    'w-full rounded-lg border border-gray-200 px-3 py-2 text-sm text-gray-800 outline-none transition-all placeholder:text-gray-400 focus:border-primary focus:ring-1 focus:ring-primary disabled:bg-gray-50'
  const labelClass = 'mb-1.5 block text-xs font-bold text-gray-500'
  const sectionHeaderClass =
    'flex items-center gap-2 border-b border-gray-100 pb-3 text-sm font-bold text-gray-800'

  return (
    <div className="animate-in fade-in fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm duration-200">
      <div className="flex h-[92vh] w-full max-w-4xl flex-col overflow-hidden rounded-2xl bg-white shadow-2xl ring-1 ring-gray-200">
        {/* 헤더 */}
        <div className="flex shrink-0 items-center justify-between border-b border-gray-100 bg-white px-8 py-5">
          <div className="flex items-center gap-3">
            <div className="bg-primary/10 text-primary flex h-10 w-10 items-center justify-center rounded-full">
              <Upload className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-gray-900">
                전처리 정보 수정
              </h3>
              <p className="text-xs text-gray-500">
                패시지 분리 정보를 수정합니다.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            disabled={isLoading}
            className="rounded-full p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600 disabled:opacity-50"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        {/* 본문 (스크롤) */}
        <div className="scrollbar-thin scrollbar-thumb-gray-200 scrollbar-track-transparent flex-1 overflow-y-auto bg-white p-8">
          <div className="flex flex-col gap-10">
            <section className="flex flex-col gap-5">
              <h4 className={sectionHeaderClass}>
                <Settings className="text-primary h-4 w-4" /> 전처리 타입 설정
              </h4>
              <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
                <div>
                  <label className={labelClass}>전처리 타입</label>
                  <select
                    name="selectType"
                    value={formData.selectType}
                    onChange={(e) => handleChangeSelectType(e.target.value)}
                    className={`${inputClass} cursor-pointer bg-white`}
                  >
                    <option value="SELECT-TYPE-EMPTY">미등록</option>
                    <option value="SELECT-TYPE-NONE">지정 안함</option>
                    <option value="SELECT-TYPE-TOKEN">토큰 수</option>
                    <option value="SELECT-TYPE-REGEX">정규식</option>
                  </select>
                </div>
                {(formData.selectType === 'SELECT-TYPE-TOKEN' ||
                  formData.selectType === 'SELECT-TYPE-REGEX') && (
                  <>
                    <div>
                      <label className={labelClass}>토큰 수 (글자 수)</label>
                      <input
                        type="number"
                        name="maxToken"
                        value={formData.maxToken}
                        onChange={(e) =>
                          handleChangeMaxToken(Number(e.target.value))
                        }
                        className={inputClass}
                      />
                    </div>
                    <div>
                      <label className={labelClass}>오버랩 크기</label>
                      <input
                        type="number"
                        name="overlapSize"
                        value={formData.overlapSize}
                        onChange={(e) =>
                          handleChangeOverlap(Number(e.target.value))
                        }
                        className={inputClass}
                      />
                    </div>
                  </>
                )}
              </div>
              {formData.selectType === 'SELECT-TYPE-REGEX' && (
                <>
                  <div className="mt-2 flex flex-col gap-4 rounded-xl border border-blue-100 bg-blue-50/50 p-5">
                    <div className="flex items-center gap-2 text-xs font-bold text-blue-700">
                      <Settings className="h-3 w-3" /> 정규식 계층 구조 설정
                    </div>
                    {([0, 1, 2] as const).map((depth) => (
                      <div key={depth} className="flex flex-col gap-3">
                        <div className="flex items-center justify-between">
                          <span className="text-[10px] font-bold tracking-wider text-gray-500 uppercase">
                            Level {depth} Patterns
                          </span>
                          <button
                            onClick={() => handleAddPattern(depth)}
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
                            (prefix, index) => (
                              <div
                                key={index}
                                className="animate-in fade-in slide-in-from-top-1 flex items-center gap-2 duration-200"
                              >
                                <div className="relative flex-1">
                                  <span className="absolute top-1/2 left-3 -translate-y-1/2 text-xs font-bold text-gray-400">
                                    /
                                  </span>
                                  <input
                                    type="text"
                                    placeholder="^제\d+조"
                                    value={prefix.prefix}
                                    onChange={(e) =>
                                      handleChangePrefix(
                                        depth,
                                        index,
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
                                    checked={prefix.isTitle}
                                    onChange={(e) =>
                                      handleChangeIsTitle(
                                        depth,
                                        index,
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
                                  onClick={() =>
                                    handleRemovePattern(depth, index)
                                  }
                                  className="flex h-8 w-8 items-center justify-center rounded-md border border-transparent text-gray-400 transition-colors hover:border-red-100 hover:bg-red-50 hover:text-red-500"
                                >
                                  <Trash2 className="h-4 w-4" />
                                </button>
                              </div>
                            ),
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                  {/* Stop Patterns */}
                  <div className="rounded-xl border border-red-100 bg-red-50/50 p-4">
                    <div className="mb-3 flex items-center justify-between">
                      <div className="flex items-center gap-2 text-xs font-bold text-red-700">
                        <X className="h-3 w-3" /> 중단 패턴
                      </div>
                      <button
                        onClick={handleAddStopPattern}
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
                            onChange={(e) =>
                              handleChangeStopPattern(idx, e.target.value)
                            }
                            className="w-full rounded-md border border-gray-200 px-3 py-1.5 text-xs outline-none focus:border-red-400 focus:ring-1 focus:ring-red-400"
                          />
                          <button
                            onClick={() => handleRemoveStopPattern(idx)}
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
                </>
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
            onClick={handleModify}
            disabled={isLoading}
            className="bg-primary hover:bg-primary-hover flex items-center gap-2 rounded-lg px-6 py-2.5 text-sm font-bold text-white shadow-md transition-all hover:shadow-lg active:scale-95 disabled:cursor-not-allowed disabled:bg-gray-300"
          >
            {isLoading ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                수정중...
              </>
            ) : (
              '수정'
            )}
          </button>
        </div>
      </div>
    </div>
  )
}
