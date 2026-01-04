'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import {
  FileText,
  Database,
  Layers,
  Bot,
  Sparkles,
  Zap,
  Activity,
  ArrowRight,
  FileSearch,
  Languages,
  FileCode,
  CheckCircle2,
  XCircle,
  Clock,
  RefreshCw,
  FlaskConical,
  Search,
  SearchCode,
} from 'lucide-react'
import { getSourceTotalCountApi } from '@/api/source'
import { getUserName } from '@/public/ts/storageUtil'

// ###################################################
// 상수 및 타입 정의 (Constants & Types)
// ###################################################

interface DashboardStats {
  totalDocuments: number
  totalPassages: number
  totalChunks: number
}

export default function HomePage() {
  const router = useRouter()

  // ###################################################
  // 상태 정의 (State)
  // ###################################################
  const [stats, setStats] = useState<DashboardStats>({
    totalDocuments: 0,
    totalPassages: 0,
    totalChunks: 0,
  })
  const [isLoading, setIsLoading] = useState(true)

  // ###################################################
  // 이펙트 및 로직 (Effects)
  // ###################################################
  useEffect(() => {
    const loadData = async () => {
      await getSourceTotalCountApi()
        .then((response) => {
          setStats({
            totalDocuments: response.result.sourceTotalCount,
            totalPassages: response.result.passageTotalCount,
            totalChunks: response.result.chunkTotalCount,
          })
        })
        .catch((error) => {
          console.error('Failed to load dashboard data', error)
        })
      setIsLoading(false)
    }
    loadData()
  }, [])

  // ###################################################
  // 렌더링 (Render)
  // ###################################################
  return (
    <div className="flex h-full w-full flex-col overflow-y-auto bg-gray-50/50 p-8">
      {/* 1. 헤더 영역 */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800">
          안녕하세요, {getUserName()} 님 👋
        </h1>
        <p className="mt-2 text-gray-500">
          문서 처리 현황을 확인하고 작업을 시작하세요.
        </p>
      </div>

      {/* 2. 통계 카드 영역 (KPIs) - Primary 컬러 테마 적용 */}
      <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        <StatCard
          title="총 문서 (Sources)"
          value={stats.totalDocuments.toLocaleString()}
          icon={<FileText className="text-primary h-6 w-6" />}
          isLoading={isLoading}
        />
        <StatCard
          title="총 패시지 (Passages)"
          value={stats.totalChunks.toLocaleString()}
          icon={<Layers className="text-primary h-6 w-6" />}
          isLoading={isLoading}
        />
        <StatCard
          title="총 청크 (Chunks)"
          value={stats.totalChunks.toLocaleString()}
          icon={<Layers className="text-primary h-6 w-6" />}
          isLoading={isLoading}
        />
      </div>

      <div className="grid flex-1 grid-cols-1 gap-8 lg:grid-cols-3">
        {/* 3. 빠른 실행 (Quick Actions) */}
        <div className="flex flex-col gap-6 lg:col-span-4">
          <h3 className="flex items-center gap-2 text-lg font-bold text-gray-700">
            <Sparkles className="text-primary h-5 w-5" />
            빠른 실행 (Quick Actions)
          </h3>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <QuickActionCard
              title="문서 추출"
              desc="HWP/PDF 텍스트 추출 테스트"
              icon={
                <FileCode className="group-hover:text-primary h-6 w-6 text-gray-600" />
              }
              color="bg-white border border-gray-200 hover:border-primary/50"
              textColor="text-gray-800"
              onClick={() => router.push('/extract/text')}
            />
            <QuickActionCard
              title="파일 문서 청킹"
              desc="HWP/PDF 문서 청크 분리 테스트"
              icon={
                <FileText className="group-hover:text-primary h-6 w-6 text-gray-600" />
              }
              color="bg-white border border-gray-200 hover:border-primary/50"
              textColor="text-gray-800"
              onClick={() => router.push('/extract/file')}
            />
            <QuickActionCard
              title="원격 문서 청킹"
              desc="원격 문서 청크 분리 테스트"
              icon={
                <FileText className="group-hover:text-primary h-6 w-6 text-gray-600" />
              }
              color="bg-white border border-gray-200 hover:border-primary/50"
              textColor="text-gray-800"
              onClick={() => router.push('/extract/repo')}
            />
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <QuickActionCard
              title="키워드 검색"
              desc="색인 청크 키워드 검색"
              icon={
                <SearchCode className="group-hover:text-primary h-6 w-6 text-gray-600" />
              }
              color="bg-white border border-gray-200 hover:border-primary/50"
              textColor="text-gray-800"
              onClick={() => router.push('/search/keyword')}
            />
            <QuickActionCard
              title="벡터 검색"
              desc="색인 청크 벡터 검색"
              icon={
                <Search className="group-hover:text-primary h-6 w-6 text-gray-600" />
              }
              color="bg-white border border-gray-200 hover:border-primary/50"
              textColor="text-gray-800"
              onClick={() => router.push('/search/vector')}
            />
            <QuickActionCard
              title="지식 베이스 관리"
              desc="문서 등록, 수정, 청크 관리 및 임베딩 현황을 관리합니다."
              icon={
                <Database className="group-hover:text-primary h-6 w-6 text-gray-600" />
              }
              color="bg-white border border-gray-200 hover:border-primary/50"
              textColor="text-gray-800"
              onClick={() => router.push('/source')}
            />
          </div>
        </div>
      </div>
    </div>
  )
}

// ###################################################
// [Sub Components]
// ###################################################
const StatCard = ({ title, value, icon, isLoading }: any) => (
  <div className="hover:border-primary/30 flex flex-col rounded-2xl border border-gray-100 bg-white p-6 shadow-sm transition-all hover:shadow-md">
    <div className="mb-4 flex items-center justify-between">
      <span className="text-sm font-bold text-gray-500">{title}</span>
      {/* 아이콘 배경을 Primary 연한색으로 통일 */}
      <div className="bg-primary/10 flex h-10 w-10 items-center justify-center rounded-xl">
        {icon}
      </div>
    </div>
    {isLoading ? (
      <div className="h-8 w-24 animate-pulse rounded bg-gray-200" />
    ) : (
      <div className="flex items-end gap-2">
        <span className={`text-3xl font-extrabold text-gray-800`}>{value}</span>
      </div>
    )}
  </div>
)

const QuickActionCard = ({
  title,
  desc,
  icon,
  color,
  textColor = 'text-gray-800',
  subTextColor = 'text-gray-500',
  onClick,
}: any) => (
  <div
    onClick={onClick}
    className={`group relative cursor-pointer overflow-hidden rounded-2xl p-6 shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg ${color}`}
  >
    <div className="relative z-10 flex flex-col gap-4">
      {/* 아이콘 배경 처리는 카드 색상에 따라 다르게 보일 수 있으므로 단순화 */}
      <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-black/5 backdrop-blur-sm transition-colors group-hover:bg-black/10">
        {icon}
      </div>
      <div>
        <h4 className={`text-lg font-bold transition-colors ${textColor}`}>
          {title}
        </h4>
        <p className={`text-sm transition-colors ${subTextColor}`}>{desc}</p>
      </div>
    </div>
  </div>
)

const StatusBadge = ({ status }: { status: string }) => {
  // 상태별 색상 (의미 전달을 위해 Primary만 쓰지 않고 유지하되 Processing은 Primary로 변경)
  let badgeStyle = 'bg-gray-100 text-gray-600'
  let icon = null

  switch (status) {
    case 'Complete':
      badgeStyle =
        'bg-green-50 text-green-700 ring-1 ring-inset ring-green-600/20'
      icon = <CheckCircle2 className="h-3 w-3" />
      break
    case 'Processing':
      // 기존 파란색 -> Primary 색상으로 변경
      badgeStyle =
        'bg-primary/10 text-primary ring-1 ring-inset ring-primary/20'
      icon = <RefreshCw className="h-3 w-3 animate-spin" />
      break
    case 'Failed':
      badgeStyle = 'bg-red-50 text-red-700 ring-1 ring-inset ring-red-600/10'
      icon = <XCircle className="h-3 w-3" />
      break
  }

  return (
    <span
      className={`inline-flex shrink-0 items-center gap-1 rounded-full px-2 py-1 text-[10px] font-medium ${badgeStyle}`}
    >
      {icon}
      {status}
    </span>
  )
}
