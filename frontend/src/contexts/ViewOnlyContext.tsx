import { createContext, useContext, type ReactNode } from 'react'

export const VIEW_ONLY_TOOLTIP = 'Read-only while viewing as another user'

const ViewOnlyContext = createContext(false)

/** Marks everything below as view-only: controls that would change data get disabled. */
export function ViewOnlyProvider({ viewOnly, children }: { viewOnly: boolean; children: ReactNode }) {
  return <ViewOnlyContext.Provider value={viewOnly}>{children}</ViewOnlyContext.Provider>
}

export function useViewOnly(): boolean {
  return useContext(ViewOnlyContext)
}
