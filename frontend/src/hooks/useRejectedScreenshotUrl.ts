import { useEffect, useState } from 'react'
import { fetchScreenshotUrl } from '../api/claimApi'

export function useRejectedScreenshotUrl(storageKey: string | undefined): string | null {
  const [url, setUrl] = useState<string | null>(null)
  useEffect(() => {
    if (!storageKey) return
    let objectUrl: string | null = null
    fetchScreenshotUrl(storageKey)
      .then(u => { objectUrl = u; setUrl(u) })
      .catch(() => {})
    return () => { if (objectUrl) URL.revokeObjectURL(objectUrl) }
  }, [storageKey])
  return url
}
