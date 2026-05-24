interface Props {
  totalBudget: number
  totalSpent: number
  totalConversions: number
  activeCount: number
}

function SummaryCard({ label, value, accent }: { label: string; value: string | number; accent: string }) {
  return (
    <div className={[
      'rounded-xl border p-4 bg-surface-light-card dark:bg-surface-dark-card',
      'border-surface-light-border dark:border-surface-dark-border',
    ].join(' ')}>
      <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{label}</p>
      <p className={['text-xl font-bold mt-1', accent].join(' ')}>{value}</p>
    </div>
  )
}

export function CampaignSummaryCards({ totalBudget, totalSpent, totalConversions, activeCount }: Props) {
  return (
    <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
      <SummaryCard label="Total Budget"  value={`₹${(totalBudget / 1000).toFixed(0)}K`}    accent="text-neon-blue" />
      <SummaryCard label="Total Spent"   value={`₹${(totalSpent / 1000).toFixed(0)}K`}     accent="text-neon-orange" />
      <SummaryCard label="Conversions"   value={totalConversions.toLocaleString()}          accent="text-neon-green" />
      <SummaryCard label="Active Now"    value={activeCount}                                accent="text-neon-purple" />
    </div>
  )
}
