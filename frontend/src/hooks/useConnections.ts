import { useState, useEffect } from 'react'
import { fetchConnections } from '../api/connectionApi'

export interface ConnectionOption {
  id: string
  name: string
}

export function useConnections(enabled = true) {
  const [connections, setConnections] = useState<ConnectionOption[]>([])
  const [loading, setLoading] = useState(enabled)

  useEffect(() => {
    if (!enabled) return
    fetchConnections('connected')
      .then(cs => {
        const seen = new Set<string>()
        setConnections(
          cs
            .filter(c => c.toUserId && !seen.has(c.toUserId) && seen.add(c.toUserId))
            .map(c => ({ id: c.toUserId, name: c.name }))
        )
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [enabled])

  return { connections, loading }
}
