import { IconPlus, IconLink } from '../icons'
import { Button } from '../Button'
import { Tabs } from '../Tabs'
import type { ConnectionDirection } from '../../../api/connectionApi'
import type { ConnectionTabLabels } from '../../../utils/connectionTabLabels'

interface ConnectionsHeaderProps {
  total: number
  connectedCount: number
  pendingCount: number
  direction: ConnectionDirection
  onDirectionChange: (direction: ConnectionDirection) => void
  tabLabels: ConnectionTabLabels
  onAddConnection: () => void
  onRequestConnection: () => void
}

interface SummaryCardProps {
  label: string
  value: string | number
  accent: string
}

function SummaryCard({ label, value, accent }: SummaryCardProps) {
  return (
    <div className="rounded-xl border p-4 bg-surface-light-card dark:bg-surface-dark-card border-surface-light-border dark:border-surface-dark-border">
      <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{label}</p>
      <p className={['text-xl font-bold mt-1', accent].join(' ')}>{value}</p>
    </div>
  )
}

export function ConnectionsHeader({
  total,
  connectedCount,
  pendingCount,
  direction,
  onDirectionChange,
  tabLabels,
  onAddConnection,
  onRequestConnection,
}: ConnectionsHeaderProps) {
  const directionTabs = [
    ...(tabLabels.child !== null ? [{ value: 'child' as const, label: tabLabels.child.label }] : []),
    ...(tabLabels.parent !== null ? [{ value: 'parent' as const, label: tabLabels.parent.label }] : []),
  ]

  return (
    <>
      <div className="flex items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            My Network
          </h1>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="yellowSoft" onClick={onRequestConnection} leftIcon={<IconLink size={14} />}>
            Request to Connect
          </Button>
          {tabLabels.child !== null && (
            <Button variant="primary" onClick={onAddConnection} leftIcon={<IconPlus size={14} />}>
              Invite
            </Button>
          )}
        </div>
      </div>

      <Tabs options={directionTabs} value={direction} onChange={onDirectionChange} />

      <div className="grid grid-cols-3 gap-4">
        <SummaryCard label="Total"     value={total}          accent="text-neon-blue"   />
        <SummaryCard label="Connected" value={connectedCount} accent="text-neon-green"  />
        <SummaryCard label="Pending"   value={pendingCount}   accent="text-neon-yellow" />
      </div>
    </>
  )
}
