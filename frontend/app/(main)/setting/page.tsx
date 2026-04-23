'use client'

import { Suspense } from 'react'
import FeatureLanding from '@/components/common/FeatureLanding'
import NotFound from '@/components/NotFound'
import { menuInfos } from '@/public/const/menu'

function SettingContent() {
  const menuInfo = menuInfos.setting
  return (
    <FeatureLanding
      icon={menuInfo.icon}
      title={menuInfo.name}
      description="인증, 문서 처리, 검색 운영에 필요한 관리 설정이 모이는 시스템 관제 진입 화면입니다."
      eyebrow="System Settings"
      summary="현재는 실제 설정 폼보다 운영 개념을 먼저 정리하고 있습니다. 추후 환경값, 권한 정책, 배치 및 프롬프트 설정이 이 영역에 합류할 수 있습니다."
      status="운영 설정 준비 중"
      statusTone="planned"
      actions={[
        {
          href: '/source',
          title: '문서 운영 상태 확인',
          description: '설정 변경 전 문서·청크 상태를 먼저 점검합니다.',
        },
        {
          href: '/prompt',
          title: '프롬프트 정책 확인',
          description: '응답 전략과 운영 설정이 만나는 구간을 확인합니다.',
        },
        {
          href: '/',
          title: '대시보드로 복귀',
          description: '전체 지표와 주요 동선을 다시 확인합니다.',
        },
      ]}
      highlights={[
        '설정 메뉴는 인증, 검색, 추출 정책의 운영 관문 역할을 합니다.',
        '현재는 향후 확장을 고려한 랜딩 구조를 먼저 제공합니다.',
        '기존 기능 페이지를 건드리지 않고 상위 진입 경험만 정리합니다.',
      ]}
    />
  )
}

export default function SettingPage() {
  return (
    <Suspense fallback={<NotFound />}>
      <SettingContent />
    </Suspense>
  )
}
