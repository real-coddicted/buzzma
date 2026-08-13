import { useEffect } from 'react'
import { useBreadcrumb } from '../../../contexts/BreadcrumbContext'
import { ShareableCampaignsTab } from './ShareableCampaignsTab'
import type { Connection } from '../../../types/ConnectionTypes'

interface Props {
  connection: Connection
  onBack: () => void
}

export function ShareCampaignView({ connection, onBack }: Props) {
  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(`Share Campaigns with ${connection.name}`, onBack)
    return clearDetail
  }, [connection.name, onBack, setDetail, clearDetail])

  return (
    <div className="max-w-7xl mx-auto flex flex-col gap-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Share Campaigns with {connection.name}
        </h1>
      </div>

      <ShareableCampaignsTab connection={connection} />
    </div>
  )
}
