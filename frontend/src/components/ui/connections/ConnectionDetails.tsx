import { useState } from 'react'
import { IconChevronRight } from '../icons'
import { ConnectionDetailsTabs } from './ConnectionDetailsTabs'
import { ConnectionProfileTab } from './ConnectionProfileTab'
import { ConnectionActivityTab } from './ConnectionActivityTab'
import type { ConnectionDetailsTab } from './ConnectionDetailsTabs'
import type { Connection } from '../../../types/ConnectionTypes'

interface ConnectionDetailsProps {
  connection: Connection
  onBack: () => void
}

export function ConnectionDetails({ connection, onBack }: ConnectionDetailsProps) {
  const [tab, setTab] = useState<ConnectionDetailsTab>('profile')

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div className="flex items-center gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted">
        <button onClick={onBack} className="hover:text-neon-blue transition-colors">
          Connections
        </button>
        <IconChevronRight size={12} />
        <span className="text-ink-light-primary dark:text-ink-dark-primary font-medium truncate">
          {connection.name}
        </span>
      </div>

      <ConnectionDetailsTabs value={tab} onChange={setTab} />

      {tab === 'profile' && (
        <ConnectionProfileTab toUserId={connection.toUserId} name={connection.name} />
      )}
      {tab === 'activity' && (
        <ConnectionActivityTab toUserId={connection.toUserId} />
      )}
    </div>
  )
}
