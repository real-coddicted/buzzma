import { useState, useCallback, useEffect } from 'react'
import { ConnectionDetailsTabs } from './ConnectionDetailsTabs'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'
import { ConnectionProfileTab } from './ConnectionProfileTab'
import { ConnectionActivityTab } from './ConnectionActivityTab'
import { Toast } from '../Toast'
import type { ConnectionDetailsTab } from './ConnectionDetailsTabs'
import type { Connection } from '../../../types/ConnectionTypes'

interface ConnectionDetailsProps {
  connection: Connection
  onBack: () => void
}

export function ConnectionDetails({ connection, onBack }: ConnectionDetailsProps) {
  const [tab, setTab]       = useState<ConnectionDetailsTab>('profile')
  const [error, setError]   = useState<string | null>(null)

  const handleError = useCallback((message: string) => setError(message), [])

  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(connection.name, onBack)
    return clearDetail
  }, [connection.name, onBack, setDetail, clearDetail])

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <ConnectionDetailsTabs value={tab} onChange={setTab} />

      {tab === 'profile' && (
        <ConnectionProfileTab toUserId={connection.toUserId} name={connection.name} onError={handleError} />
      )}
      {tab === 'activity' && (
        <ConnectionActivityTab toUserId={connection.toUserId} onError={handleError} />
      )}

      {error && (
        <Toast message={error} type="error" onDismiss={() => setError(null)} />
      )}
    </div>
  )
}
