'use client'

import '@/public/css/globals.css'
import React, { useState, useEffect } from 'react'
import Sidebar from '@/components/Sidebar'
import Header from '@/components/Header'
import AuthGuard from '@/components/auth/AuthGuard'
import LayoutWrapper from '@/components/LayoutWrapper'

export default function MainLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <AuthGuard>
      <LayoutWrapper>{children}</LayoutWrapper>
    </AuthGuard>
  )
}
