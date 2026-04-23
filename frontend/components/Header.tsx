'use client'

import { useRouter } from 'next/navigation'
import { User, LogOut } from 'lucide-react'
import { useAuthStore } from '@/stores/authStore'

export default function Header() {
  const router = useRouter()
  const username = useAuthStore((s) => s.username)
  const clearAuth = useAuthStore((s) => s.clearAuth)

  const handleLogout = async () => {
    if (!confirm('로그아웃 하시겠습니까?')) return

    clearAuth()
    router.replace('/login')
  }

  return (
    <header className="bg-primary z-10 flex h-10 shrink-0 items-center justify-end px-4 text-white shadow-md">
      <div className="flex items-center gap-3">
        {username && (
          <div className="flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-xs backdrop-blur-sm">
            <User className="h-3.5 w-3.5 opacity-80" />
            <span className="font-medium">{username} 님</span>
          </div>
        )}

        {username && <div className="h-3 w-px bg-white/20"></div>}

        <button
          onClick={handleLogout}
          className="flex items-center gap-1.5 rounded-md px-2 py-1 text-xs font-medium text-white/90 transition-colors hover:bg-white/20 hover:text-white active:scale-95"
          title="로그아웃"
        >
          <LogOut className="h-3.5 w-3.5" />
          <span>로그아웃</span>
        </button>
      </div>
    </header>
  )
}
