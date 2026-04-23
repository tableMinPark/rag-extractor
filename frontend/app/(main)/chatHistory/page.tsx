'use client'

import { Suspense } from 'react'
import FeatureLanding from '@/components/common/FeatureLanding'
import NotFound from '@/components/NotFound'
import { menuInfos } from '@/public/const/menu'

function ChatHistoryContent() {
  const menuInfo = menuInfos.chatHistory
  return (
    <FeatureLanding
      icon={menuInfo.icon}
      title={menuInfo.name}
      description="사용자 질문 흐름과 응답 품질을 되짚는 용도의 이력 관리 영역입니다."
      eyebrow="History"
      summary="현재는 실제 이력 리스트보다 진입 구조와 향후 확장 위치를 먼저 맞추고 있습니다. 추후 검색 이력과 질의 로그가 이 구간에 합류할 수 있습니다."
      status="이력 화면 준비 중"
      statusTone="planned"
      actions={[
        {
          href: '/search/keyword',
          title: '최근 검색 흐름 재현',
          description: '키워드 검색 화면에서 동일 조건을 다시 실행해 결과를 비교합니다.',
        },
        {
          href: '/search/vector',
          title: '벡터 검색 흐름 재현',
          description: '유사도 기반 검색 결과를 다시 확인하며 응답 품질을 검토합니다.',
        },
        {
          href: '/',
          title: '대시보드로 이동',
          description: '전체 작업 흐름과 지표를 다시 확인한 뒤 다음 작업을 선택합니다.',
        },
      ]}
      highlights={[
        '이력 화면은 추후 질문·응답 로그 통합 영역으로 확장될 수 있습니다.',
        '현재는 검색 기반 시나리오를 재실행하는 허브 역할에 집중합니다.',
        '기존 라우팅을 바꾸지 않고 기능 위치만 명확히 보여줍니다.',
      ]}
    />
  )
}

export default function ChatHistoryPage() {
  return (
    <Suspense fallback={<NotFound />}>
      <ChatHistoryContent />
    </Suspense>
  )
}
