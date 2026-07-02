import { useState, useEffect } from 'react'
import { Loading } from '../Loading'
import { StatRow } from '../StatRow'
import { fetchUserActivity } from '../../../api/userApi'
import type { UserActivityDto } from '../../../types/ProfileTypes'

interface ConnectionActivityTabProps {
  toUserId: string
}

export function ConnectionActivityTab({ toUserId }: ConnectionActivityTabProps) {
  const [activity, setActivity] = useState<UserActivityDto | null>(null)
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetchUserActivity(toUserId)
      .then(d => { if (!cancelled) setActivity(d) })
      .catch(() => { if (!cancelled) setActivity(null) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [toUserId])

  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5 max-w-md">
      <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-4">
        Activity
      </h3>
      {loading ? (
        <div className="flex justify-center py-6">
          <Loading size={24} />
        </div>
      ) : activity ? (
        <div>
          <StatRow label="Number of Orders"  value={activity.orderCount} />
          <StatRow label="Total Connections" value={activity.connectionCount} />
        </div>
      ) : (
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
          Activity data not available.
        </p>
      )}
    </div>
  )
}
