'use client'

import { Suspense } from 'react'
import FeatureLanding from '@/components/common/FeatureLanding'
import NotFound from '@/components/NotFound'
import { menuInfos } from '@/public/const/menu'

function DocumentContent() {
  const menuInfo = menuInfos.document
  return (
    <FeatureLanding
      icon={menuInfo.icon}
      title={menuInfo.name}
      description="등록된 참고 문서와 대상 문서를 기준으로 전체 지식 베이스 흐름을 빠르게 파악하는 진입 화면입니다."
      eyebrow="Document Overview"
      summary="현재 저장소에는 문서 조회 전용 상세 화면이 아직 크지 않기 때문에, 이 구간에서는 문서 관리와 검색 화면으로 빠르게 이어지는 동선을 우선 제공합니다."
      status="문서 조회 흐름 준비 완료"
      statusTone="ready"
      actions={[
        {
          href: '/source',
          title: '문서 관리로 이동',
          description: '등록된 문서, 패시지, 청크 현황을 바로 확인합니다.',
        },
        {
          href: '/search/keyword',
          title: '키워드 검색 실행',
          description: '문서 내용을 기반으로 즉시 검색 결과를 탐색합니다.',
        },
        {
          href: '/search/vector',
          title: '벡터 검색 실행',
          description: '임베딩된 청크 기준으로 유사 문서를 탐색합니다.',
        },
      ]}
      highlights={[
        '문서 조회 메뉴는 지식 베이스 입구 역할에 집중합니다.',
        '상세 내용 확인은 기존 문서 관리와 검색 화면을 그대로 재사용합니다.',
        '새 화면을 추가하지 않고 현재 라우팅을 유지한 채 연결만 정리합니다.',
      ]}
    />
  )
}

export default function DocumentPage() {
  return (
    <Suspense fallback={<NotFound />}>
      <DocumentContent />
    </Suspense>
  )
}
