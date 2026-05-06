import { IconPlus } from '../icons'

interface ConnectionsHeaderProps {
  total: number
  connectedCount: number
  pendingCount: number
  onAddConnection: () => void
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
  onAddConnection,
}: ConnectionsHeaderProps) {
  return (
    <>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Connections
          </h1>
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            {total} total · {connectedCount} connected
          </p>
        </div>
        <button
          onClick={onAddConnection}
          className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-lg bg-neon-blue text-surface-dark-base hover:brightness-110 transition-all shadow-neon-blue/30"
        >
          <IconPlus size={14} />
          Add Connection
        </button>
      </div>

      <div className="grid grid-cols-3 gap-4">
        <SummaryCard label="Total"     value={total}          accent="text-neon-blue"   />
        <SummaryCard label="Connected" value={connectedCount} accent="text-neon-green"  />
        <SummaryCard label="Pending"   value={pendingCount}   accent="text-neon-yellow" />
      </div>
    </>
  )
}
