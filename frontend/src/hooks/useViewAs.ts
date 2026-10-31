import { useSyncExternalStore } from 'react'
import { getViewAsUserId, subscribeViewAs } from '../api/client'

/** The user the admin is currently viewing as, or null. */
export function useViewAsUserId(): string | null {
  return useSyncExternalStore(subscribeViewAs, getViewAsUserId)
}
