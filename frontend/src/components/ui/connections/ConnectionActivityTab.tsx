import { useState, useEffect } from 'react'
import { Loading } from '../Loading'
import { StatRow } from '../StatRow'
import { fetchUserActivity } from '../../../api/userApi'
import type { UserActivityDto } from '../../../types/ProfileTypes'

interface ConnectionActivityTabProps {
  toUserId: string
  onError: (message: string) => void
}

export function ConnectionActivityTab({ toUserId, onError }: ConnectionActivityTabProps) {
  const [activity, setActivity] = useState<UserActivityDto | null>(null)
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetchUserActivity(toUserId)
      .then(d => { if (!cancelled) setActivity(d) })
      .catch((err: unknown) => {
        if (!cancelled) {
          setActivity(null)
          onError(err instanceof Error ? err.message : 'Failed to load activity.')
        }
      })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [toUserId, onError])

  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5 max-w-md">
      <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-4">
        Activity
      </h3>
      {loading ? (
        <div className="flex justify-center py-6">
          <Loading size={24} />
        </div>
      ) : (
        <div>
          <StatRow label="Number of Orders"  value={activity?.orderCount ?? '—'} />
          <StatRow label="Total Connections" value={activity?.connectionCount ?? '—'} />
        </div>
      )}
    </div>
  )
}
