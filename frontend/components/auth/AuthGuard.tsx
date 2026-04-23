'use client'

import { useEffect, useState } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { config } from '@/public/ts/config'
import { useAuthStore } from '@/stores/authStore'

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const accessToken = useAuthStore((state) => state.accessToken)
  const [isMounted, setIsMounted] = useState(false)
  const isLoginPage = pathname === '/login' || pathname === `${config.basePath}/login`

  useEffect(() => {
    setIsMounted(true)
  }, [])

  useEffect(() => {
    if (!isMounted) {
      return
    }

    if (!isLoginPage && !accessToken) {
      router.replace('/login')
    }
  }, [accessToken, isLoginPage, isMounted, router])

  if (!isMounted) {
    return null
  }

  if (isLoginPage) {
    return <>{children}</>
  }

  if (!accessToken) {
    return null
  }

  return <>{children}</>
}
