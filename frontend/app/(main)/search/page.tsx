'use client'

import { Suspense } from 'react'
import FeatureLanding from '@/components/common/FeatureLanding'
import NotFound from '@/components/NotFound'
import { menuInfos } from '@/public/const/menu'

function SearchContent() {
  const menuInfo = menuInfos.search
  return (
    <FeatureLanding
      icon={menuInfo.icon}
      title={menuInfo.name}
      description="문서 청크를 탐색할 때 가장 자주 들어오는 검색 허브입니다. 키워드와 벡터 검색을 같은 흐름 안에서 빠르게 전환할 수 있도록 구성했습니다."
      eyebrow="Search Hub"
      summary="정확한 문자열 매칭이 필요한 경우에는 키워드 검색, 의미 유사도 기반 탐색이 필요한 경우에는 벡터 검색으로 바로 이동하면 됩니다."
      status="검색 허브 활성화"
      statusTone="ready"
      actions={[
        {
          href: '/search/keyword',
          title: '키워드 검색',
          description: '정확한 용어, 규정명, 문장 일부를 기준으로 빠르게 찾습니다.',
        },
        {
          href: '/search/vector',
          title: '벡터 검색',
          description: '질문과 의미가 비슷한 청크를 임베딩 기반으로 찾습니다.',
        },
        {
          href: '/source',
          title: '문서 관리 보기',
          description: '검색 대상이 되는 문서와 청크 상태를 먼저 점검합니다.',
        },
      ]}
      highlights={[
        '검색 진입점은 하나로 유지하고 실제 실행 화면만 분기합니다.',
        '기존 키워드/벡터 검색 페이지는 그대로 유지됩니다.',
        '검색 전 문서 관리에서 색인 대상과 카테고리를 확인할 수 있습니다.',
      ]}
    />
  )
}

export default function SearchPage() {
  return (
    <Suspense fallback={<NotFound />}>
      <SearchContent />
    </Suspense>
  )
}
