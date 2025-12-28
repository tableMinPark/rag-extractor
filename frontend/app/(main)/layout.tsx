'use client'

import '@/public/css/globals.css'
import React, { useState, useEffect } from 'react'
import Sidebar from '@/components/Sidebar'
import Header from '@/components/Header'
import AuthGuard from '@/components/auth/AuthGuard'

export default function MainLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const [isSidebarOpen, setIsSidebarOpen] = useState(true)

  useEffect(() => {
    const savedState = localStorage.getItem('sidebarOpen')
    if (savedState !== null) {
      setIsSidebarOpen(savedState === 'true')
    }
  }, [])

  const toggleSidebar = () => {
    const newState = !isSidebarOpen
    setIsSidebarOpen(newState)
    localStorage.setItem('sidebarOpen', String(newState))
  }

  return (
    <AuthGuard>
      <div className="flex h-screen w-full overflow-hidden bg-gray-50">
        <Sidebar isOpen={isSidebarOpen} onToggle={toggleSidebar} />
        <div className="flex h-full w-full flex-1 flex-col overflow-hidden">
          <Header />
          <main className="relative flex-1 overflow-y-auto bg-white">
            {children}
          </main>
        </div>
      </div>
    </AuthGuard>
  )
}
