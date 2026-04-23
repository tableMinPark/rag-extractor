import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  accessToken: string | null
  userId: string | null
  name: string | null
  role: string | null
  setAuth: (accessToken: string, userId: string, name: string, role?: string) => void
  clearAuth: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      userId: null,
      name: null,
      role: null,
      setAuth: (accessToken, userId, name, role = '') =>
        set({ accessToken, userId, name, role }),
      clearAuth: () =>
        set({ accessToken: null, userId: null, name: null, role: null }),
    }),
    { name: 'auth' },
  ),
)
