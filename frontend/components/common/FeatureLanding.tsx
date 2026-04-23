'use client'

import Link from 'next/link'
import { ArrowRight, CheckCircle2, LucideIcon } from 'lucide-react'

type FeatureAction = {
  href: string
  title: string
  description: string
}

type FeatureLandingProps = {
  icon: LucideIcon
  title: string
  description: string
  eyebrow: string
  summary: string
  status: string
  statusTone?: 'ready' | 'progress' | 'planned'
  actions: FeatureAction[]
  highlights: string[]
}

const statusToneClassMap = {
  ready: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
  progress: 'bg-amber-50 text-amber-700 ring-amber-200',
  planned: 'bg-slate-100 text-slate-600 ring-slate-200',
}

export default function FeatureLanding({
  icon: Icon,
  title,
  description,
  eyebrow,
  summary,
  status,
  statusTone = 'ready',
  actions,
  highlights,
}: FeatureLandingProps) {
  return (
    <div className="flex h-full w-full flex-col overflow-y-auto bg-gray-50/80 p-8">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-8">
        <section className="grid gap-6 lg:grid-cols-[minmax(0,1.5fr)_minmax(320px,0.7fr)]">
          <div className="relative overflow-hidden rounded-[28px] bg-white p-8 shadow-xl shadow-gray-200/60 ring-1 ring-gray-100">
            <div className="absolute -top-14 -right-14 h-40 w-40 rounded-full bg-primary/8" />
            <div className="absolute right-12 bottom-8 h-24 w-24 rounded-full bg-primary/6" />
            <div className="relative z-10 flex flex-col gap-5">
              <div className="inline-flex w-fit items-center gap-2 rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold tracking-[0.16em] text-primary uppercase">
                <Icon className="h-3.5 w-3.5" />
                {eyebrow}
              </div>
              <div className="space-y-3">
                <h1 className="text-3xl font-extrabold tracking-tight text-gray-900 sm:text-4xl">
                  {title}
                </h1>
                <p className="max-w-3xl text-sm leading-7 text-gray-500 sm:text-base">
                  {description}
                </p>
              </div>
              <div className="rounded-2xl border border-primary/10 bg-gradient-to-r from-primary/8 via-white to-white px-5 py-4">
                <p className="text-sm leading-7 text-gray-600">{summary}</p>
              </div>
            </div>
          </div>

          <div className="flex flex-col justify-between rounded-[28px] border border-gray-200 bg-white p-6 shadow-lg shadow-gray-200/40">
            <div className="space-y-4">
              <span
                className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ring-1 ${statusToneClassMap[statusTone]}`}
              >
                {status}
              </span>
              <div className="space-y-2">
                <h2 className="text-lg font-bold text-gray-900">핵심 안내</h2>
                <p className="text-sm leading-6 text-gray-500">
                  현재 화면은 기능 진입 전에 필요한 작업 흐름과 이동 지점을 빠르게 안내합니다.
                </p>
              </div>
            </div>

            <div className="mt-6 space-y-3">
              {highlights.map((highlight) => (
                <div
                  key={highlight}
                  className="flex items-start gap-3 rounded-2xl bg-gray-50 px-4 py-3"
                >
                  <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
                  <p className="text-sm leading-6 text-gray-600">{highlight}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="space-y-4">
          <div className="space-y-1">
            <h2 className="text-xl font-bold text-gray-900">빠른 이동</h2>
            <p className="text-sm text-gray-500">
              현재 구조를 유지한 채 바로 이어지는 화면으로 이동할 수 있습니다.
            </p>
          </div>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {actions.map((action) => (
              <Link
                key={action.href}
                href={action.href}
                className="group rounded-3xl border border-gray-200 bg-white p-6 shadow-sm transition-all hover:-translate-y-1 hover:border-primary/40 hover:shadow-lg"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="space-y-2">
                    <h3 className="text-base font-bold text-gray-900">
                      {action.title}
                    </h3>
                    <p className="text-sm leading-6 text-gray-500">
                      {action.description}
                    </p>
                  </div>
                  <span className="rounded-full bg-primary/10 p-2 text-primary transition-colors group-hover:bg-primary group-hover:text-white">
                    <ArrowRight className="h-4 w-4" />
                  </span>
                </div>
              </Link>
            ))}
          </div>
        </section>
      </div>
    </div>
  )
}
