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