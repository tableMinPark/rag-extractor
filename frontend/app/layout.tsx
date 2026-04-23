import '@/public/css/globals.css'
import type { Metadata } from 'next'
import LayoutWrapper from '@/components/LayoutWrapper'
import Providers from './providers'

export const metadata: Metadata = {
  title: 'RAG Extractor',
  description: 'RAG Extractor Management',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <Providers>
      <html lang="ko">
        <body className="font-sans flex min-h-screen bg-white">
          <LayoutWrapper>{children}</LayoutWrapper>
        </body>
      </html>
    </Providers>
  )
}
