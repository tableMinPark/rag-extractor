'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { User, LogOut } from 'lucide-react'

interface HeaderProps {}

export default function Header({}: HeaderProps) {
  const router = useRouter()
  const [username, setUsername] = useState<string>('')

  // 컴포넌트 마운트 시 로컬스토리지에서 사용자 이름 가져오기
  useEffect(() => {
    // Next.js Hydration Mismatch 방지를 위해 useEffect 안에서 접근
    const storedUsername = localStorage.getItem('username')
    if (storedUsername) {
      setUsername(storedUsername)
    }
  }, [])

  const handleLogout = async () => {
    if (!confirm('로그아웃 하시겠습니까?')) return

    // 1. 로컬 스토리지 정보 삭제
    localStorage.removeItem('accessToken')
    localStorage.removeItem('username')
    localStorage.removeItem('role')

    // 2. 로그인 페이지로 이동 (history replace로 뒤로가기 방지)
    router.replace('/login')
  }

  return (
    <header className="bg-primary z-10 flex h-10 shrink-0 items-center justify-between px-4 text-white shadow-md">
      {/* 좌측 영역 (로고나 메뉴 토글 등) */}
      <div className="flex items-center font-bold">RAG Management System</div>

      {/* 우측 영역: 사용자 정보 및 로그아웃 */}
      <div className="flex items-center gap-3">
        {/* 사용자 정보 표시 */}
        {username && (
          <div className="flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-xs backdrop-blur-sm">
            <User className="h-3.5 w-3.5 opacity-80" />
            <span className="font-medium">{username} 님</span>
          </div>
        )}

        {/* 구분선 (사용자 이름이 있을 때만 표시) */}
        {username && <div className="h-3 w-px bg-white/20"></div>}

        {/* 로그아웃 버튼 */}
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
