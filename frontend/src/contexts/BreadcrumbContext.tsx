import { createContext, useContext, useState, useCallback, type ReactNode } from 'react'

interface BreadcrumbContextValue {
  detailTitle: string | null
  onDetailBack: (() => void) | null
  setDetail: (title: string, onBack: () => void) => void
  clearDetail: () => void
}

const BreadcrumbContext = createContext<BreadcrumbContextValue>({
  detailTitle: null,
  onDetailBack: null,
  setDetail: () => {},
  clearDetail: () => {},
})

export function BreadcrumbProvider({ children }: { children: ReactNode }) {
  const [detailTitle, setDetailTitle] = useState<string | null>(null)
  const [onDetailBack, setOnDetailBack] = useState<(() => void) | null>(null)

  const setDetail = useCallback((title: string, onBack: () => void) => {
    setDetailTitle(title)
    setOnDetailBack(() => onBack)
  }, [])

  const clearDetail = useCallback(() => {
    setDetailTitle(null)
    setOnDetailBack(null)
  }, [])

  return (
    <BreadcrumbContext.Provider value={{ detailTitle, onDetailBack, setDetail, clearDetail }}>
      {children}
    </BreadcrumbContext.Provider>
  )
}

export function useBreadcrumb() {
  return useContext(BreadcrumbContext)
}
