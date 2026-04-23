'use client'

import { useEffect, useState } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { config } from '@/public/ts/config'

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname() // 현재 경로 확인용
  const [isAuthorized, setIsAuthorized] = useState(false)

  useEffect(() => {
    if (config.mode === 'development') {
      setIsAuthorized(true)
      return
    }

    // ★ [추가] 현재 페이지가 '/login'이면 아무것도 하지 않고 통과
    if (pathname === '/login' || pathname === `${config.basePath}/login`) {
      setIsAuthorized(true)
      return
    }

    const accessToken = localStorage.getItem('accessToken')

    if (!accessToken) {
      router.replace('/login')
    } else {
      setIsAuthorized(true)
    }
  }, [router, pathname])

  // 인증 확인 중일 때 (로그인 페이지는 바로 보여줌)
  if (!isAuthorized) {
    return null
  }

  return <>{children}</>
}
