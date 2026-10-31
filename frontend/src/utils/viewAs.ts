// Admin "view as": the user an admin is looking at lives in the URL as ?viewAs=<uuid>
// and is sent to the backend as the X-View-As-User header.

export const VIEW_AS_PARAM = 'viewAs'
export const VIEW_AS_HEADER = 'X-View-As-User'

export function readViewAsParam(search: string): string | null {
  return new URLSearchParams(search).get(VIEW_AS_PARAM) || null
}

/** Returns `search` with the viewAs param set to `userId`, or removed when `userId` is null. */
export function withViewAsParam(search: string, userId: string | null): string {
  const params = new URLSearchParams(search)
  if (userId) params.set(VIEW_AS_PARAM, userId)
  else params.delete(VIEW_AS_PARAM)
  const next = params.toString()
  return next ? `?${next}` : ''
}

/** Only admins can view as another user; for anyone else the request is ignored. */
export function resolveViewAs(userId: string | null, role: string | null | undefined): string | null {
  return userId && role === 'ROLE_ADMIN' ? userId : null
}
