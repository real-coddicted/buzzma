import { scorePillClass } from '../ui/claim-review/claimUtils'

export function ScoreBadge({ score, className = 'ml-1.5' }: { score: number | null | undefined; className?: string }) {
  if (score == null) return null
  const pct = score
  return (
    <span className={`${className} px-1.5 py-0.5 rounded text-[10px] font-semibold ${scorePillClass(pct)}`}>
      {pct}%
    </span>
  )
}
