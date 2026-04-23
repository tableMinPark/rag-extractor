import { useAuthStore } from '@/stores/authStore'

export const getRole = () => useAuthStore.getState().role ?? ''

export const getUserName = () => useAuthStore.getState().username ?? ''
