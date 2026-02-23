import '@/public/css/globals.css'
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import Providers from './providers'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'RAG Admin System',
  description: 'AI Knowledge Base Management',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <Providers>
      <html lang="ko">
        <body className={`flex ${inter.className}`}>{children}</body>
      </html>
    </Providers>
  )
}
