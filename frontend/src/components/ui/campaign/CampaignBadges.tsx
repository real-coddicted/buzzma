import { PLATFORM_COLORS, TYPE_COLORS } from './filters/chipColors'
import { PLATFORM_LABELS, CAMPAIGN_TYPE_LABELS } from '../../../constants/campaigns'
import type { Platform, CampaignType } from '../../../types'

export function PlatformBadge({ platform }: { platform: string }) {
  const colors = PLATFORM_COLORS[platform as Platform]
  if (!colors) return <span className="text-ink-light-muted dark:text-ink-dark-muted">{platform}</span>
  return (
    <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', colors.base].join(' ')}>
      {PLATFORM_LABELS[platform as Platform]}
    </span>
  )
}

export function DealTypeBadge({ campaignType }: { campaignType: string | null }) {
  if (!campaignType) return <span className="text-ink-light-muted dark:text-ink-dark-muted">—</span>
  const colors = TYPE_COLORS[campaignType as CampaignType]
  if (!colors) return <span className="text-ink-light-muted dark:text-ink-dark-muted">{campaignType}</span>
  return (
    <span className={['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', colors.base].join(' ')}>
      {CAMPAIGN_TYPE_LABELS[campaignType as CampaignType]}
    </span>
  )
}

/** Progress bar + claimed/total fraction for a campaign's slot usage. */
export function SlotsBar({ claimed, total }: { claimed: number; total: number }) {
  const pct = total > 0 ? Math.min(100, Math.round((claimed / total) * 100)) : 0
  const color = pct >= 90 ? 'bg-neon-red' : pct >= 70 ? 'bg-neon-orange' : 'bg-neon-blue'
  return (
    <div className="space-y-1">
      <span className="font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
        {claimed}/{total}
      </span>
      <div className="flex items-center gap-2">
        <div className="w-20 h-1.5 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
          <div className={['h-full rounded-full', color].join(' ')} style={{ width: `${pct}%` }} />
        </div>
        <span className={['text-[10px] font-semibold tabular-nums', pct >= 90 ? 'text-neon-red' : pct >= 70 ? 'text-neon-orange' : 'text-ink-light-muted dark:text-ink-dark-muted'].join(' ')}>
          {pct}%
        </span>
      </div>
    </div>
  )
}