'use client'

import { useEffect, useState } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { config } from '@/public/ts/config'
import { useAuthStore } from '@/stores/authStore'

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter()
  const pathname = usePathname()
  const [isAuthorized, setIsAuthorized] = useState(false)
  const accessToken = useAuthStore((s) => s.accessToken)

  useEffect(() => {
    if (pathname === '/login' || pathname === `${config.basePath}/login`) {
      setIsAuthorized(true)
      return
    }

    if (config.mode === 'local') {
      setIsAuthorized(true)
    } else if (!accessToken) {
      router.replace('/login')
    } else {
      setIsAuthorized(true)
    }
  }, [router, pathname, accessToken])

  if (!isAuthorized) {
    return null
  }

  return <>{children}</>
}
