'use client'

import React, { useState } from 'react'
import { useRouter } from 'next/navigation'
import {
  Lock,
  User,
  Loader2,
  ShieldCheck,
  ArrowRight,
  AlertCircle,
} from 'lucide-react'
import { loginApi } from '@/api/auth'
import { useAuthStore } from '@/stores/authStore'

export default function LoginPage() {
  const router = useRouter()
  const setAuth = useAuthStore((s) => s.setAuth)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    if (errorMessage) setErrorMessage(null)
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()

    // 유효성 검사
    if (!formData.username || !formData.password) {
      setErrorMessage('아이디와 비밀번호를 모두 입력해주세요.')
      return
    }

    setIsLoading(true)
    setErrorMessage(null)

    await loginApi(formData.username, formData.password)
      .then((response) => {
        // 로그인 성공
        if (response.code === 0) {
          setAuth(response.result.accessToken, response.result.username, response.result.role)
          router.push('/')
        }
      })
      .catch((error) => {
        if (error.status === 401) {
          setErrorMessage('아이디 또는 비밀번호가 일치하지 않습니다.')
        } else {
          setErrorMessage('서버와 통신이 원할하지 않습니다.')
        }
      })

    setIsLoading(false)
  }

  return (
    <div className="flex min-h-screen w-full items-center justify-center bg-gray-50 p-6">
      <div className="animate-in fade-in zoom-in-95 slide-in-from-bottom-2 w-full max-w-md duration-500">
        {/* 헤더 영역 */}
        <div className="mb-8 flex flex-col items-center text-center">
          <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white shadow-lg ring-1 ring-gray-100">
            <ShieldCheck className="text-primary fill-primary/10 h-8 w-8" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">
            RAG Management System
          </h1>
          <p className="mt-2 text-sm text-gray-500">
            계정으로 접속하여 시스템을 관리하세요.
          </p>
        </div>

        {/* 로그인 폼 카드 */}
        <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-xl shadow-gray-200/50">
          {/* [수정] div 대신 form 태그 사용 + onSubmit 연결 */}
          <form onSubmit={handleLogin} className="p-8">
            <div className="flex flex-col gap-5">
              {/* 에러 메시지 */}
              {errorMessage && (
                <div className="animate-in slide-in-from-top-1 fade-in flex items-center gap-2 rounded-lg bg-red-50 p-3 text-sm text-red-600 duration-200">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{errorMessage}</span>
                </div>
              )}

              {/* 아이디 */}
              <div>
                <label className="mb-1.5 block text-xs font-bold text-gray-600">
                  아이디 (ID)
                </label>
                <div className="relative">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                    <User className="h-5 w-5 text-gray-400" />
                  </div>
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    // onKeyDown 삭제 (form이 알아서 처리함)
                    placeholder="admin"
                    className={`focus:border-primary focus:ring-primary block w-full rounded-lg border bg-gray-50 py-2.5 pr-3 pl-10 text-sm text-gray-900 transition-all outline-none placeholder:text-gray-400 focus:bg-white focus:ring-1 ${
                      errorMessage
                        ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                        : 'border-gray-200'
                    }`}
                    autoComplete="username"
                  />
                </div>
              </div>

              {/* 비밀번호 */}
              <div>
                <div className="mb-1.5 flex items-center justify-between">
                  <label className="text-xs font-bold text-gray-600">
                    비밀번호 (Password)
                  </label>
                </div>
                <div className="relative">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                    <Lock className="h-5 w-5 text-gray-400" />
                  </div>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    // onKeyDown 삭제
                    placeholder="••••••••"
                    className={`focus:border-primary focus:ring-primary block w-full rounded-lg border bg-gray-50 py-2.5 pr-3 pl-10 text-sm text-gray-900 transition-all outline-none placeholder:text-gray-400 focus:bg-white focus:ring-1 ${
                      errorMessage
                        ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                        : 'border-gray-200'
                    }`}
                    autoComplete="current-password"
                  />
                </div>
              </div>

              {/* 로그인 버튼 */}
              <button
                type="submit" // [수정] 다시 submit으로 변경 (그래야 엔터키가 먹음)
                disabled={isLoading || !formData.username || !formData.password}
                className="group bg-primary hover:bg-primary-hover relative flex w-full items-center justify-center gap-2 rounded-lg py-3 text-sm font-bold text-white shadow-md transition-all hover:shadow-lg active:scale-95 disabled:cursor-not-allowed disabled:bg-gray-300 disabled:shadow-none"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    로그인 중...
                  </>
                ) : (
                  <>
                    로그인
                    <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
