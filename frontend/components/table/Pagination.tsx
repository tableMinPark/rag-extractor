import React, { useMemo } from 'react'
import { ArrowLeftIcon, ArrowRightIcon } from 'lucide-react'

interface PaginationProps {
  page: number
  totalPage: number
  onPageChange: (page: number) => void
  maxVisible?: number // 기본 5
}

export const Pagination: React.FC<PaginationProps> = ({
  page: currentPage,
  totalPage,
  onPageChange,
  maxVisible = 3,
}) => {
  const pages = useMemo(() => {
    const half = Math.floor(maxVisible / 2)

    let start = currentPage - half
    let end = currentPage + half

    if (start < 1) {
      start = 1
      end = Math.min(maxVisible, totalPage)
    }

    if (end > totalPage) {
      end = totalPage
      start = Math.max(1, totalPage - maxVisible + 1)
    }

    return Array.from({ length: end - start + 1 }, (_, i) => start + i)
  }, [currentPage, totalPage, maxVisible])

  if (totalPage <= 1) return null

  return (
    <div className="flex justify-between gap-4 rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
      <div className="flex items-center">
        {/* 이전 버튼 */}
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 1}
          className="rounded border border-gray-300 bg-white px-4 py-2 text-sm text-gray-600 shadow-sm hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <ArrowLeftIcon className="h-4 w-4" />
        </button>
      </div>
      <div className="flex items-center gap-2">
        {/* 첫 페이지 생략 처리 */}
        {pages[0] > 1 && (
          <>
            <button
              onClick={() => onPageChange(1)}
              className="rounded border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-100"
            >
              <span className="w-10 text-center">1</span>
            </button>
            {pages[0] > 2 && <span className="px-2 text-sm">...</span>}
          </>
        )}

        {/* 페이지 버튼들 */}
        {pages.map((page) => (
          <button
            key={page}
            onClick={() => onPageChange(page)}
            className={`rounded px-4 py-2 text-sm font-medium transition ${
              page === currentPage
                ? 'bg-gray-400 text-white'
                : 'border border-gray-300 bg-white text-gray-700 hover:bg-gray-100'
            }`}
          >
            <span className="w-10 text-center">{page}</span>
          </button>
        ))}

        {/* 마지막 페이지 생략 처리 */}
        {pages[pages.length - 1] < totalPage && (
          <>
            {pages[pages.length - 1] < totalPage - 1 && (
              <span className="px-2 text-sm">...</span>
            )}
            <button
              onClick={() => onPageChange(totalPage)}
              className="rounded border border-gray-300 bg-white px-4 py-2 text-sm hover:bg-gray-50"
            >
              <span className="w-10 text-center">{totalPage}</span>
            </button>
          </>
        )}
      </div>
      <div className="flex items-center">
        {/* 다음 버튼 */}
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage === totalPage}
          className="rounded border border-gray-300 bg-white px-4 py-2 text-sm text-gray-600 shadow-sm hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <ArrowRightIcon className="h-4 w-4" />
        </button>
      </div>
    </div>
  )
}
