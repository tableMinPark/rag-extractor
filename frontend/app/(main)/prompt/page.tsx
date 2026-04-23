'use client'

import { Suspense } from 'react'
import FeatureLanding from '@/components/common/FeatureLanding'
import NotFound from '@/components/NotFound'
import { menuInfos } from '@/public/const/menu'

function PromptContent() {
  const menuInfo = menuInfos.prompt
  return (
    <FeatureLanding
      icon={menuInfo.icon}
      title={menuInfo.name}
      description="검색 결과를 어떤 방식으로 요약하고 답변화할지 제어하는 프롬프트 운영 구간입니다."
      eyebrow="Prompt Control"
      summary="현재 프로젝트에서는 실제 프롬프트 편집기를 붙이기 전에, 프롬프트 관리가 검색·시뮬레이션과 어떻게 이어지는지부터 화면 구조에 반영하고 있습니다."
      status="프롬프트 관리 확장 예정"
      statusTone="progress"
      actions={[
        {
          href: '/simulation',
          title: '시뮬레이션 흐름 확인',
          description: '프롬프트 변화가 필요한 실험 시나리오를 먼저 정리합니다.',
        },
        {
          href: '/search/keyword',
          title: '검색 결과 확인',
          description: '프롬프트 설계 전에 어떤 근거 문맥이 들어오는지 점검합니다.',
        },
        {
          href: '/setting',
          title: '운영 설정 확인',
          description: '프롬프트 운영과 함께 바뀌는 시스템 설정 범위를 확인합니다.',
        },
      ]}
      highlights={[
        '프롬프트 관리는 향후 답변 품질 제어의 중심 화면이 됩니다.',
        '현재는 검색과 시뮬레이션 흐름 사이의 연결 지점으로 정리합니다.',
        '새 편집 화면이 들어와도 기존 메뉴 구조는 그대로 유지됩니다.',
      ]}
    />
  )
}

export default function PromptPage() {
  return (
    <Suspense fallback={<NotFound />}>
      <PromptContent />
    </Suspense>
  )
}
