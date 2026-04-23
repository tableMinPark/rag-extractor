'use client'

import { Suspense } from 'react'
import FeatureLanding from '@/components/common/FeatureLanding'
import NotFound from '@/components/NotFound'
import { menuInfos } from '@/public/const/menu'

function SimulationContent() {
  const menuInfo = menuInfos.simulation
  return (
    <FeatureLanding
      icon={menuInfo.icon}
      title={menuInfo.name}
      description="질문, 프롬프트, 참고 문맥을 바꿔가며 추출 결과와 검색 품질을 검증하는 실험용 진입 구간입니다."
      eyebrow="Simulation"
      summary="현재 프로젝트에서는 실제 시뮬레이션 로직을 단계적으로 맞추는 중이라, 이 화면은 향후 실험 패널의 기준 톤과 이동 구조를 먼저 맞추는 역할을 합니다."
      status="시뮬레이션 단계적 확장"
      statusTone="progress"
      actions={[
        {
          href: '/search/keyword',
          title: '검색 결과 점검',
          description: '검색 결과 품질을 먼저 확인한 뒤 시뮬레이션 가정을 세웁니다.',
        },
        {
          href: '/search/vector',
          title: '유사도 탐색 점검',
          description: '벡터 검색 결과를 기준으로 답변 문맥 구성을 검토합니다.',
        },
        {
          href: '/source',
          title: '지식 베이스 점검',
          description: '문서와 청크 데이터 상태를 확인해 실험 전제를 맞춥니다.',
        },
      ]}
      highlights={[
        '시뮬레이션 메뉴는 추후 품질 검증 패널의 기준 화면이 됩니다.',
        '현재는 검색과 문서 관리 화면을 중심으로 실험 흐름을 이어갈 수 있습니다.',
        '레이아웃 톤은 rag-genAI 스타일을 따르되, 기존 라우트는 그대로 유지합니다.',
      ]}
    />
  )
}

export default function SimulationPage() {
  return (
    <Suspense fallback={<NotFound />}>
      <SimulationContent />
    </Suspense>
  )
}
