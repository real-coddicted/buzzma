export function ScoreBadge({ score }: { score: number | null | undefined }) {
  if (score == null) return null
  const pct = Math.round(score * 100)
  const cls = score >= 0.7
    ? 'text-neon-green bg-neon-green/10'
    : score >= 0.4
      ? 'text-amber-400 bg-amber-400/10'
      : 'text-neon-red bg-neon-red/10'
  return (
    <span className={`ml-1.5 px-1.5 py-0.5 rounded text-[10px] font-semibold ${cls}`}>
      {pct}%
    </span>
  )
}
