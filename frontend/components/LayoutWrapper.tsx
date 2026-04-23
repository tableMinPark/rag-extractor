'use client'

import React, { useEffect, useState } from 'react'
import Sidebar from '@/components/Sidebar'
import Header from '@/components/Header'
import { UiStatus, useUiStore } from '@/stores/uiStore'
import Loading from './common/Loading'
import Error from './common/Error'
import { usePathname } from 'next/navigation'
import { ModalType, useModalStore } from '@/stores/modalStore'
import ModalInfo from './modal/ModalInfo'
import ModalConfirm from './modal/ModalConfirm'
import { config } from '@/public/ts/config'

export default function LayoutWrapper({
  children,
}: {
  children: React.ReactNode
}) {
  const pathname = usePathname()

  const resetUi = useUiStore((s) => s.reset)
  const uiStatus = useUiStore((s) => s.status)
  const handleCancel = useUiStore((s) => s.handleCancel)
  const handleRefresh = useUiStore((s) => s.handleRefresh)

  const resetModal = useModalStore((m) => m.reset)
  const modalIsOpen = useModalStore((m) => m.isOpen)
  const modalType = useModalStore((m) => m.type)

  const normalizedPathname =
    config.basePath && pathname.startsWith(config.basePath)
      ? pathname.slice(config.basePath.length) || '/'
      : pathname
  const isAuthPage =
    normalizedPathname === '/login' || normalizedPathname === '/register'

  const [isSidebarOpen, setIsSidebarOpen] = useState(() => {
    if (typeof window === 'undefined') {
      return true
    }

    const savedState = window.localStorage.getItem('sidebarOpen')
    return savedState !== null ? savedState === 'true' : true
  })

  /**
   * 경로 변경 시 UI 상태 초기화
   */
  useEffect(() => {
    resetUi()
    resetModal()
  }, [pathname, resetModal, resetUi])

  /**
   * 사이드바 토글 핸들러
   */
  const handleToggleSidebar = () => {
    const newState = !isSidebarOpen
    setIsSidebarOpen(newState)
    window.localStorage.setItem('sidebarOpen', String(newState))
  }

  /**
   * UI 상태에 따른 화면 렌더링
   * @param uiStatus UI상태
   * @returns UI 상태에 따른 화면
   */
  function renderAlert(uiStatus: UiStatus) {
    switch (uiStatus) {
      case 'loading':
        return <Loading onCancel={handleCancel} />

      case 'error':
        return <Error onRefresh={handleRefresh} />

      default:
        return null
    }
  }

  /**
   * 모달 상태에 따른 화면 렌더링
   * @param modalType 모달 타입
   * @returns 모달 상태에 따른 화면
   */
  function renderModal(modalType: ModalType) {
    switch (modalType) {
      case 'info':
        return <ModalInfo />

      case 'confirm':
        return <ModalConfirm />

      default:
        return null
    }
  }

  if (isAuthPage) {
    return <>{children}</>
  }

  return (
    <>
      <Sidebar isOpen={isSidebarOpen} onToggle={handleToggleSidebar} />
      <div className="flex h-full w-full flex-1 flex-col overflow-hidden">
        <Header />
        <main className="relative flex-1 overflow-y-auto bg-white">
          {/* 라우팅 컴포넌트 */}
          {children}
        </main>
      </div>
      {/* UI 상태에 따른 컴포넌트 */}
      {uiStatus !== 'idle' && (
        <div className="absolute z-50 h-full w-full">
          {renderAlert(uiStatus)}
        </div>
      )}
      {/* 모달 상태에 따른 컴포넌트 */}
      {modalIsOpen && (
        <div className="absolute z-100 h-full w-full">
          {renderModal(modalType)}
        </div>
      )}
    </>
  )
}
