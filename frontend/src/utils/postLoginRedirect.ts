const AUTH_PATHS = ['/login', '/register', '/forgot-password', '/reset-password']

export const POST_LOGIN_REDIRECT_KEY = 'postLoginRedirect'

export function isAuthPath(pathname: string): boolean {
  return AUTH_PATHS.includes(pathname)
}
