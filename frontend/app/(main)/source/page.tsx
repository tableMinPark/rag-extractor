'use client'

import { Suspense, useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import NotFound from '@/components/NotFound'
import { menuInfos } from '@/public/const/menu'
import { getRole } from '@/public/ts/storageUtil'
import {
  ApproveType,
  Category,
  SelectType,
  Source,
  TableOption,
} from '@/types/domain'
import {
  AlertCircle,
  EditIcon,
  Loader2,
  Plus,
  RefreshCw,
  TrashIcon,
} from 'lucide-react'
import { useUiStore } from '@/stores/uiStore'
import { useModalStore } from '@/stores/modalStore'
import { Pagination } from '@/components/table/Pagination'
import { ModalCreateSource } from '@/components/modal/ModalCreateSource'
import { ModalModifySource } from '@/components/modal/ModalModifySource'
import { ModalModifySelectType } from '@/components/modal/ModalModifySelectType'
import {
  getCategoriesSourceApi,
  getSourcesApi,
  deleteSourceApi,
  updateIsBatchApi,
} from '@/api/source'
import { batchPassagingApi } from '@/api/batch'

const DEFAULT_APPROVE_TYPES: ApproveType[] = [
  {
    code: 'REQUEST',
    name: '승인요청',
    color: 'blue',
  },
  {
    code: 'ALLOW',
    name: '승인',
    color: 'green',
  },
  {
    code: 'DENY',
    name: '반려',
    color: 'red',
  },
  {
    code: 'WAIT',
    name: '대기',
    color: 'gray',
  },
]

const EMPTY_SELECT_CODE = 'SELECT-TYPE-EMPTY'
const DEFAULT_SELECT_TYPES: SelectType[] = [
  {
    code: 'SELECT-TYPE-EMPTY',
    name: '미등록',
    color: 'red',
  },
  {
    code: 'SELECT-TYPE-TOKEN',
    name: '토큰',
    color: 'green',
  },
  {
    code: 'SELECT-TYPE-REGEX',
    name: '정규식',
    color: 'blue',
  },
  {
    code: 'SELECT-TYPE-NONE',
    name: '지정안함',
    color: 'gray',
  },
]

interface SourceTableOption extends TableOption {
  keyword: string
  categoryCode: string
  approveCode: string
}

function DocumentContent() {
  const menuInfo = menuInfos.source
  const uiStore = useUiStore()
  const modalStore = useModalStore()
  const router = useRouter()

  // ###################################################
  // 상태 관리
  // ###################################################
  const [isLoading, setIsLoading] = useState(true)
  const [isError, setIsError] = useState(false)
  const [modifyModalIsOpen, setModifyModalIsOpen] = useState(false)
  const [createModalIsOpen, setCreateModalIsOpen] = useState(false)
  const [modifySelectTypeModalIsOpen, setModifySelectTypeModalIsOpen] =
    useState(false)
  const [currentSourceId, setCurrentSourceId] = useState<number | null>(null)
  const [sources, setSources] = useState<Source[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [totalPage, setTotalPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [tableOption, setTableOption] = useState<SourceTableOption>({
    page: 1,
    size: 10,
    keyword: '',
    categoryCode: 'ALL',
    approveCode: 'ALL',
  })

  // ###################################################
  // 랜더링 이펙트
  // ###################################################
  useEffect(() => {
    handleGetCategories()
    handleGetSources()
  }, [])

  useEffect(() => {
    handleGetSources()
  }, [tableOption.page])

  // ###################################################
  // 핸들러
  // ###################################################
  const handleGetCategories = async () => {
    try {
      const response = await getCategoriesSourceApi()
      setCategories(response.result)
    } catch (e) {
      console.error('카테고리 조회 실패', e)
    }
  }

  const handleGetSources = async () => {
    setIsLoading(true)
    setIsError(false)
    try {
      const keyword =
        tableOption.keyword.trim() !== '' ? tableOption.keyword : undefined
      const categoryCode =
        tableOption.categoryCode !== 'ALL' ? tableOption.categoryCode : undefined
      const response = await getSourcesApi(
        tableOption.page,
        tableOption.size,
        keyword,
        categoryCode,
      )
      setSources(
        response.result.content.map((s) => ({ ...s, selectCode: s.selectType })),
      )
      setTotalCount(response.result.totalCount)
      setTotalPage(response.result.totalPages)
    } catch (e) {
      console.error('문서 목록 조회 실패', e)
      setIsError(true)
    } finally {
      setIsLoading(false)
    }
  }

  /**
   * 검색어 변경 이벤트 핸들러
   *
   * @param keyword 검색어
   */
  const handleChangeKeyword = (keyword: string) => {
    setTableOption((prev) => {
      return {
        ...prev,
        keyword: keyword,
      }
    })
  }

  /**
   * 대상 문서 클릭 이벤트 핸들러
   *
   * @param source 대상 문서
   */
  const handleClickSource = (source: Source) => {
    if (source.selectCode === EMPTY_SELECT_CODE) {
      modalStore.setInfo(
        '전처리 타입 등록 필요',
        '전처리 타입이 등록되지 않았습니다.',
        '패시지 관리를 위해서는 전처리 타입 등록이 필요합니다.',
      )
      return
    }
    router.push(`/source/detail?sourceId=${source.sourceId}`)
  }

  const handleBatchPassaging = async () => {
    modalStore.setConfirm(
      '패시지 분리 배치',
      '배치 문서의 패시지 분리 배치를 실행하시겠습니까?',
      '배치 대상 문서만 실행되며, 이전 버전의 패시지는 삭제됩니다.',
      async () => {
        try {
          await batchPassagingApi()
          await handleGetSources()
        } catch (e) {
          console.error('패시지 분리 배치 실패', e)
        }
      },
    )
  }

  /**
   * 대상 문서 수정 핸들러
   *
   * @param sourceId 대상 문서 ID
   */
  const handleModifySource = (sourceId: number) => {
    setCurrentSourceId(sourceId)
    setModifyModalIsOpen(true)
    console.log('문서 수정 모달 오픈')
  }

  const handleDeleteSource = (sourceId: number) => {
    modalStore.setConfirm(
      '문서 삭제',
      '정말로 문서를 삭제하시겠습니까?',
      '삭제한 문서는 복구할 수 없습니다.',
      async () => {
        try {
          await deleteSourceApi(sourceId)
          await handleGetSources()
        } catch (e) {
          console.error('문서 삭제 실패', e)
        }
      },
    )
  }

  /**
   * 대상 문서 전처리 타입 수정 핸들러
   *
   * @param sourceId 대상 문서 ID
   */
  const handleModifySelectType = (sourceId: number) => {
    setCurrentSourceId(sourceId)
    setModifySelectTypeModalIsOpen(true)
    console.log('문서 전처리 방식 수정 모달 오픈')
  }

  const handleToggleIsBatch = async (sourceId: number, isBatch: boolean) => {
    setSources((prev) =>
      prev.map((source) =>
        source.sourceId === sourceId ? { ...source, isBatch } : source,
      ),
    )
    try {
      await updateIsBatchApi(sourceId, isBatch)
    } catch (e) {
      console.error('배치 여부 수정 실패', e)
      setSources((prev) =>
        prev.map((source) =>
          source.sourceId === sourceId ? { ...source, isBatch: !isBatch } : source,
        ),
      )
    }
  }

  /**
   * 승인 코드 배지 Element 생성 핸들러
   *
   * @param approveCode 승인/반려 코드
   * @param sourceId 대상 문서 ID
   * @returns 승인/반려 배지 Element
   */
  const handleApproveCode = (approveCode: string, sourceId: number) => {
    let badge = <></>
    DEFAULT_APPROVE_TYPES.forEach((type: ApproveType) => {
      if (approveCode === type.code) {
        badge = (
          <span
            className={`inline-flex items-center rounded-md bg-${type.color}-50 px-2 py-1 text-sm font-medium text-${type.color}-700 ring-1 ring-${type.color}-700/10 ring-inset`}
          >
            {type.name}
          </span>
        )
      }
    })
    return badge
  }

  /**
   * 전처리 타입 배지 Element 생성 핸들러
   *
   * @param selectCode 전처리 타입 코드
   * @param sourceId 대상 문서 ID
   * @returns 전처리 타입 배지 Element
   */
  const handleSelectCode = (selectCode: string, sourceId: number) => {
    let badge = <></>
    DEFAULT_SELECT_TYPES.forEach((type: SelectType) => {
      if (selectCode === type.code) {
        badge = (
          <span
            className={`hover:bg-${type.color}-100 inline-flex items-center rounded-md bg-${type.color}-50 px-2 py-1 text-sm font-medium text-${type.color}-700 ring-1 ring-${type.color}-700/10 cursor-pointer ring-inset`}
            onClick={() => handleModifySelectType(sourceId)}
          >
            {type.name}
          </span>
        )
      }
    })
    return badge
  }

  // ###################################################
  // 렌더링 (Render)
  // ###################################################
  return (
    <div className="flex h-full w-full flex-col p-6">
      {/* 헤더 영역 */}
      <div className="mb-6 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div>
            <h2 className="flex items-center gap-2 text-2xl font-bold text-gray-800">
              <menuInfo.icon className="text-primary h-6 w-6" />
              {menuInfo.name}
            </h2>
            <p className="mt-1 text-xs text-gray-500">{menuInfo.description}</p>
          </div>
        </div>
      </div>
      {/* 2. 검색 및 필터 영역 (새로 추가됨) */}
      <div className="mb-4 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
        {/* 좌측: 카테고리 셀렉트 + 검색어 입력 */}
        <div className="flex flex-1 items-center gap-3">
          {/* 카테고리 필터 셀렉트 */}
          <div className="relative">
            <select
              value={tableOption.categoryCode}
              onChange={(e) =>
                setTableOption((prev) => ({
                  ...prev,
                  categoryCode: e.target.value,
                }))
              }
              className="focus:border-primary focus:ring-primary h-10 w-35 appearance-none rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 focus:ring-1 focus:outline-none"
            >
              <option value="ALL">카테고리전체</option>
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
          <div className="relative">
            <select
              value={tableOption.approveCode}
              onChange={(e) =>
                setTableOption((prev) => ({
                  ...prev,
                  approveCode: e.target.value,
                }))
              }
              className="focus:border-primary focus:ring-primary h-10 w-35 appearance-none rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 focus:ring-1 focus:outline-none"
            >
              <option value="ALL">승인여부전체</option>
              {DEFAULT_APPROVE_TYPES.map((approveType) => (
                <option key={approveType.code} value={approveType.code}>
                  {approveType.name}
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
              value={tableOption.keyword}
              onChange={(e) => handleChangeKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleGetSources()}
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
            onClick={handleGetSources}
            className="bg-primary hover:bg-primary-hover h-10 rounded-lg px-4 py-2 text-sm font-bold text-white transition-colors"
          >
            검색
          </button>
        </div>
      </div>
      {/* 테이블 헤더 */}
      <div className="mb-4 flex w-full flex-wrap justify-between">
        <div className="flex w-full flex-1 items-end justify-start gap-3">
          <span className="text-xs text-gray-500">
            총 <span className="font-bold">{totalCount}</span>
            {` 개의 문서`}
          </span>
        </div>
        <div className="flex flex-1 items-center justify-end gap-2">
          {getRole() === 'ROLE_ADMIN' && (
            <>
              <button
                onClick={handleBatchPassaging}
                className="bg-primary hover:bg-primary-hover flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold text-white shadow-sm transition-all active:scale-95"
              >
                <RefreshCw className="h-4 w-4" />
                패시지 분리 배치
              </button>
              <button
                onClick={() => setCreateModalIsOpen(true)}
                className="bg-primary hover:bg-primary-hover flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold text-white shadow-sm transition-all active:scale-95"
              >
                <Plus className="h-4 w-4" />
                문서 등록
              </button>
            </>
          )}
        </div>
      </div>
      <div className="flex min-h-120 flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
        <table className="h-full w-full min-w-full text-left text-sm text-gray-600">
          <thead className="bg-gray-50 text-sm font-bold text-gray-500 uppercase shadow-sm">
            <tr>
              <th className="w-15 px-4 py-4 text-center">ID</th>
              <th className="px-4 py-4">문서명</th>
              <th className="w-26 px-2 py-4 text-center">카테고리</th>
              <th className="w-22 px-2 py-4 text-center">문서타입</th>
              <th className="w-27 px-2 py-4 text-center">전처리타입</th>
              <th className="w-20 px-2 py-4 text-center">버전</th>
              <th className="w-22 px-2 py-4 text-center">자동화여부</th>
              <th className="w-22 px-2 py-4 text-center">승인여부</th>
              <th className="w-23 px-2 py-4 text-center">배치여부</th>
              <th className="w-13 px-2 py-4 text-center"></th>
              <th className="w-13 px-2 py-4 text-center"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 bg-white">
            {isLoading && (
              <tr>
                <td colSpan={10} className="text-center text-gray-500">
                  <div className="flex flex-1 flex-col items-center justify-center gap-3">
                    <Loader2 className="text-primary h-8 w-8 animate-spin" />
                    <p className="text-sm font-medium text-gray-500">
                      목록을 불러오는 중입니다...
                    </p>
                  </div>
                </td>
              </tr>
            )}
            {!isLoading && isError && (
              <tr>
                <td colSpan={10} className="text-center text-gray-500">
                  <div className="flex flex-1 flex-col items-center justify-center gap-5">
                    <AlertCircle className="h-8 w-8 text-red-500" />
                    <p className="text-sm font-bold text-gray-700">
                      문서 목록 조회 실패
                    </p>
                    <button
                      onClick={handleGetSources}
                      className="flex items-center gap-2 rounded-md bg-gray-100 px-3 py-1.5 text-xs font-bold text-gray-600 hover:bg-gray-200"
                    >
                      <RefreshCw className="h-3 w-3" />
                      다시 시도
                    </button>
                  </div>
                </td>
              </tr>
            )}
            {!isLoading && !isError && (
              <>
                {sources.length === 0 && (
                  <tr>
                    <td colSpan={10} className="text-center text-gray-500">
                      등록된 문서가 없습니다.
                    </td>
                  </tr>
                )}
                {sources.map((source) => (
                  <tr
                    key={source.sourceId}
                    className="group max-h-4 transition-colors"
                  >
                    <td className="px-2 py-2 text-center font-mono text-sm text-gray-400 group-hover:text-gray-600">
                      {source.sourceId}
                    </td>
                    <td
                      onClick={() => handleClickSource(source)}
                      className="group-hover:text-primary max-w-50 cursor-pointer truncate px-2 py-2 text-sm text-gray-800 transition-colors"
                      title={source.name}
                    >
                      {source.name}
                    </td>
                    <td className="px-2 py-2 text-center">
                      <span className="inline-flex items-center rounded-md bg-gray-100 px-2 py-1 text-sm font-medium text-gray-600 ring-1 ring-gray-700/10 ring-inset">
                        {source.categoryName}
                      </span>
                    </td>
                    <td className="px-2 py-2 text-center">
                      <span className="font-mono text-sm font-bold text-gray-500">
                        {source.sourceTypeName}
                      </span>
                    </td>
                    <td className="px-2 py-2 text-center">
                      {handleSelectCode(source.selectCode, source.sourceId)}
                    </td>
                    <td className="px-2 py-2 text-center">
                      <span className="inline-flex items-center rounded-md bg-gray-100 px-2 py-1 text-sm font-medium text-gray-600 ring-1 ring-gray-700/10 ring-inset">
                        v{source.version}
                      </span>
                    </td>
                    <td className="px-2 py-2 text-center">
                      {source.isAuto ? (
                        <span className="inline-flex items-center rounded-md bg-blue-50 px-2 py-1 text-sm font-medium text-blue-700 ring-1 ring-blue-700/10 ring-inset">
                          자동
                        </span>
                      ) : (
                        <span className="inline-flex items-center rounded-md bg-yellow-50 px-2 py-1 text-sm font-medium text-yellow-800 ring-1 ring-yellow-600/20 ring-inset">
                          수동
                        </span>
                      )}
                    </td>
                    <td className="px-2 py-2 text-center">
                      {handleApproveCode(source.approveCode, source.sourceId)}
                    </td>
                    <td className="px-2 py-2 text-center">
                      <label className="relative inline-flex cursor-pointer items-center">
                        <input
                          type="checkbox"
                          checked={source.isBatch}
                          onChange={(e) =>
                            handleToggleIsBatch(
                              source.sourceId,
                              e.target.checked,
                            )
                          }
                          className="peer sr-only"
                        />
                        <div className="peer peer-checked:bg-primary peer-focus:ring-primary/20 h-6 w-11 rounded-full bg-gray-200 peer-focus:ring-2 peer-focus:outline-none after:absolute after:top-0.5 after:left-0.5 after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:after:translate-x-full peer-checked:after:border-white"></div>
                      </label>
                    </td>
                    <td className="text-sm text-gray-500">
                      <button
                        onClick={() => handleModifySource(source.sourceId)}
                        className="text-gray h-10 rounded-lg text-sm font-bold transition-colors"
                      >
                        <EditIcon className="h-6 w-6" />
                      </button>
                    </td>
                    <td className="text-sm text-gray-500">
                      <button
                        onClick={() => handleDeleteSource(source.sourceId)}
                        className="text-gray h-10 rounded-lg text-sm font-bold transition-colors"
                      >
                        <TrashIcon className="h-6 w-6" />
                      </button>
                    </td>
                  </tr>
                ))}
              </>
            )}
          </tbody>
        </table>
      </div>
      <div className="flex items-center justify-center px-6 py-3">
        <Pagination
          page={tableOption.page}
          totalPage={totalPage}
          onPageChange={(page: number) =>
            setTableOption((prev) => ({ ...prev, page }))
          }
        />
      </div>
      {createModalIsOpen && (
        <ModalCreateSource
          onClose={() => {
            handleGetSources()
            setCreateModalIsOpen(false)
          }}
        />
      )}
      {modifyModalIsOpen && currentSourceId !== null && (
        <ModalModifySource
          sourceId={currentSourceId}
          onClose={() => {
            handleGetSources()
            setModifyModalIsOpen(false)
          }}
        />
      )}
      {modifySelectTypeModalIsOpen && (
        <ModalModifySelectType
          onClose={() => {
            handleGetSources()
            setModifySelectTypeModalIsOpen(false)
          }}
        />
      )}
    </div>
  )
}

export default function DocumentPage() {
  return (
    <Suspense fallback={<NotFound />}>
      <DocumentContent />
    </Suspense>
  )
}
