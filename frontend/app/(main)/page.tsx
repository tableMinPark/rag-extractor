'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import {
  ArrowRight,
  BookOpen,
  FileSearch,
  FileText,
  Layers,
  Search,
  SearchCode,
  Sparkles,
  WandSparkles,
} from 'lucide-react'
import { getSourceTotalCountApi } from '@/api/source'
import { getUserName } from '@/public/ts/storageUtil'

interface DashboardStats {
  totalDocuments: number
  totalPassages: number
  totalChunks: number
}

type QuickAction = {
  title: string
  description: string
  href: string
  icon: typeof Sparkles
}

const quickActions: QuickAction[] = [
  {
    title: '텍스트 추출',
    description: '문서 원문에서 바로 텍스트를 뽑아 결과를 확인합니다.',
    href: '/extract/text',
    icon: WandSparkles,
  },
  {
    title: '파일 청킹',
    description: '업로드 파일을 기준으로 패시지와 청크 구조를 점검합니다.',
    href: '/extract/file',
    icon: FileText,
  },
  {
    title: '원격 문서 청킹',
    description: '원격 문서를 수집하고 청킹 흐름을 테스트합니다.',
    href: '/extract/repo',
    icon: BookOpen,
  },
  {
    title: '키워드 검색',
    description: '정확한 용어 매칭 중심으로 검색 결과를 확인합니다.',
    href: '/search/keyword',
    icon: SearchCode,
  },
  {
    title: '벡터 검색',
    description: '의미 유사도 기반으로 관련 청크를 탐색합니다.',
    href: '/search/vector',
    icon: Search,
  },
  {
    title: '문서 관리',
    description: '등록 문서, 패시지, 청크 상태를 한 번에 점검합니다.',
    href: '/source',
    icon: FileSearch,
  },
]

export default function HomePage() {
  const router = useRouter()
  const [stats, setStats] = useState<DashboardStats>({
    totalDocuments: 0,
    totalPassages: 0,
    totalChunks: 0,
  })
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const loadData = async () => {
      try {
        const response = await getSourceTotalCountApi()
        setStats({
          totalDocuments: response.result.sourceTotalCount,
          totalPassages: response.result.passageTotalCount,
          totalChunks: response.result.chunkTotalCount,
        })
      } catch (error) {
        console.error('Failed to load dashboard data', error)
      } finally {
        setIsLoading(false)
      }
    }

    loadData()
  }, [])

  return (
    <div className="flex h-full w-full flex-col overflow-y-auto bg-gray-50/80 p-8">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-8">
        <section className="grid gap-6 lg:grid-cols-[minmax(0,1.4fr)_minmax(320px,0.8fr)]">
          <div className="relative overflow-hidden rounded-[32px] bg-white p-8 shadow-xl shadow-gray-200/60 ring-1 ring-gray-100">
            <div className="absolute -top-18 -right-10 h-44 w-44 rounded-full bg-primary/8" />
            <div className="absolute right-10 bottom-8 h-28 w-28 rounded-full bg-primary/6" />
            <div className="relative z-10 flex flex-col gap-6">
              <div className="inline-flex w-fit items-center gap-2 rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold tracking-[0.16em] text-primary uppercase">
                <Sparkles className="h-3.5 w-3.5" />
                Dashboard
              </div>
              <div className="space-y-3">
                <h1 className="text-4xl font-extrabold tracking-tight text-gray-900">
                  안녕하세요, {getUserName() || '관리자'} 님
                </h1>
                <p className="max-w-3xl text-sm leading-7 text-gray-500 sm:text-base">
                  문서 추출, 청킹, 검색까지 이어지는 RAG 운영 흐름을 한 화면에서
                  빠르게 점검할 수 있습니다. 자주 쓰는 작업은 바로 실행하고,
                  지식 베이스 상태는 아래 지표에서 확인하세요.
                </p>
              </div>
              <div className="flex flex-wrap gap-3">
                <button
                  onClick={() => router.push('/source')}
                  className="bg-primary hover:bg-primary-hover inline-flex items-center gap-2 rounded-full px-5 py-3 text-sm font-semibold text-white shadow-md transition-all hover:-translate-y-0.5"
                >
                  문서 관리 열기
                  <ArrowRight className="h-4 w-4" />
                </button>
                <button
                  onClick={() => router.push('/search/keyword')}
                  className="inline-flex items-center gap-2 rounded-full border border-gray-200 bg-white px-5 py-3 text-sm font-semibold text-gray-700 transition-all hover:border-primary/30 hover:text-primary"
                >
                  검색 바로가기
                </button>
              </div>
            </div>
          </div>

          <div className="rounded-[32px] border border-gray-200 bg-white p-6 shadow-lg shadow-gray-200/40">
            <div className="mb-5 flex items-center justify-between">
              <div>
                <h2 className="text-lg font-bold text-gray-900">운영 메모</h2>
                <p className="mt-1 text-sm text-gray-500">
                  오늘 자주 쓰는 흐름을 기준으로 시작하세요.
                </p>
              </div>
              <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700 ring-1 ring-emerald-200">
                Ready
              </span>
            </div>
            <div className="space-y-3">
              <InfoCard
                title="문서 등록 후 점검"
                description="문서 관리에서 등록 상태를 확인하고 패시지·청크 구조를 이어서 점검합니다."
              />
              <InfoCard
                title="검색 품질 확인"
                description="키워드와 벡터 검색을 번갈아 실행해 실제 검색 체감 품질을 비교합니다."
              />
              <InfoCard
                title="추출 흐름 확인"
                description="텍스트 추출 결과를 확인한 뒤 파일·원격 문서 청킹으로 이어집니다."
              />
            </div>
          </div>
        </section>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <StatCard
            title="총 문서"
            value={stats.totalDocuments.toLocaleString()}
            icon={FileText}
            isLoading={isLoading}
          />
          <StatCard
            title="총 패시지"
            value={stats.totalPassages.toLocaleString()}
            icon={BookOpen}
            isLoading={isLoading}
          />
          <StatCard
            title="총 청크"
            value={stats.totalChunks.toLocaleString()}
            icon={Layers}
            isLoading={isLoading}
          />
        </section>

        <section className="space-y-4">
          <div className="space-y-1">
            <h2 className="text-xl font-bold text-gray-900">빠른 실행</h2>
            <p className="text-sm text-gray-500">
              현재 프로젝트에서 바로 사용할 수 있는 기존 기능 화면으로 연결됩니다.
            </p>
          </div>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {quickActions.map((action) => (
              <button
                key={action.href}
                onClick={() => router.push(action.href)}
                className="group rounded-[28px] border border-gray-200 bg-white p-6 text-left shadow-sm transition-all hover:-translate-y-1 hover:border-primary/40 hover:shadow-lg"
              >
                <div className="flex h-full flex-col gap-5">
                  <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary transition-colors group-hover:bg-primary group-hover:text-white">
                    <action.icon className="h-6 w-6" />
                  </div>
                  <div className="space-y-2">
                    <h3 className="text-lg font-bold text-gray-900">
                      {action.title}
                    </h3>
                    <p className="text-sm leading-6 text-gray-500">
                      {action.description}
                    </p>
                  </div>
                  <div className="mt-auto inline-flex items-center gap-2 text-sm font-semibold text-primary">
                    화면 열기
                    <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
                  </div>
                </div>
              </button>
            ))}
          </div>
        </section>
      </div>
    </div>
  )
}

function StatCard({
  title,
  value,
  icon: Icon,
  isLoading,
}: {
  title: string
  value: string
  icon: typeof Sparkles
  isLoading: boolean
}) {
  return (
    <div className="rounded-[28px] border border-gray-200 bg-white p-6 shadow-sm">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-gray-500">{title}</p>
        </div>
        <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary">
          <Icon className="h-5 w-5" />
        </div>
      </div>
      {isLoading ? (
        <div className="h-10 w-28 animate-pulse rounded-xl bg-gray-100" />
      ) : (
        <p className="text-3xl font-extrabold tracking-tight text-gray-900">
          {value}
        </p>
      )}
    </div>
  )
}

function InfoCard({
  title,
  description,
}: {
  title: string
  description: string
}) {
  return (
    <div className="rounded-2xl bg-gray-50 px-4 py-4">
      <p className="text-sm font-semibold text-gray-800">{title}</p>
      <p className="mt-1 text-sm leading-6 text-gray-500">{description}</p>
    </div>
  )
}
