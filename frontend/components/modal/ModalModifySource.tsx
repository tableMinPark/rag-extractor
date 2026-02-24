'use client'

import { useState, useEffect } from 'react'
import { Loader2, Upload, X } from 'lucide-react'

export const ModalModifySource = ({
  sourceId,
  onClose,
}: {
  sourceId: number
  onClose: () => void
}) => {
  // ###################################################
  // 상태 관리
  // ###################################################
  const [isLoading, setIsLoading] = useState(false)

  // ###################################################
  // 랜더링 이펙트
  // ###################################################
  useEffect(() => {
    handleGetSource(sourceId)
  }, [])

  // ###################################################
  // 핸들러
  // ###################################################
  const handleGetSource = async (sourceId: Number) => {
    setIsLoading(true)
    console.log(`${sourceId} 문서 조회`)
    setIsLoading(false)
  }

  const handleModifySource = async () => {
    setIsLoading(true)
    console.log('문서 수정')
    setIsLoading(false)
  }

  // ###################################################
  // 렌더링 (Render)
  // ###################################################
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
              <h3 className="text-lg font-bold text-gray-900">
                문서 정보 수정
              </h3>
              <p className="text-xs text-gray-500">
                등록된 지식 베이스 문서 메타 정보를 수정합니다.
              </p>
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
        <div className="scrollbar-thin scrollbar-thumb-gray-200 scrollbar-track-transparent flex-1 overflow-y-auto bg-white p-8"></div>

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
            onClick={handleModifySource}
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
