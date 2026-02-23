'use client'

import { Suspense } from 'react'
import NotFound from '@/components/NotFound'
import { menuInfos } from '@/public/const/menu'

function ChatHistoryContent() {
  const menuInfo = menuInfos.chatHistory

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
      {/* 채팅 영역 */}
      <div className="min-h-0 flex-1"></div>
    </div>
  )
}

export default function ChatHistoryPage() {
  return (
    <Suspense fallback={<NotFound />}>
      <ChatHistoryContent />
    </Suspense>
  )
}
