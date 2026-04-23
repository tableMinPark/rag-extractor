import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

const PUBLIC_PATHS = ['/login', '/register']

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  const hasRefreshToken = request.cookies.has('refreshToken')
  const basePath = request.nextUrl.basePath || ''
  const normalizedPathname =
    basePath && pathname.startsWith(basePath)
      ? pathname.slice(basePath.length) || '/'
      : pathname

  const isPublic = PUBLIC_PATHS.some((p) => normalizedPathname.startsWith(p))

  if (isPublic) {
    return NextResponse.next()
  }

  if (!hasRefreshToken) {
    return NextResponse.redirect(new URL(`${basePath}/login`, request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/((?!_next/static|_next/image|favicon.ico|public).*)'],
}
