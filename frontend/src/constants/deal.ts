import type { Platform, CampaignType, DealTypeFilter, DealPlatformFilter } from '../types/DealTypes'
import type { FilterOption } from '../components/ui/StatusFilterPills'
import { PLATFORM_COLORS as CHIP_PLATFORM_COLORS } from '../components/ui/campaign/filters/chipColors'

export const PLATFORM_COLORS: Record<Platform, string> = Object.fromEntries(
  Object.entries(CHIP_PLATFORM_COLORS).map(([platform, colors]) => [platform, colors.base])
) as Record<Platform, string>

export const DEAL_TYPE_COLORS: Record<CampaignType, string> = {
  CAMPAIGN_TYPE_RATING:            'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/25',
  CAMPAIGN_TYPE_REVIEW:            'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/25',
  CAMPAIGN_TYPE_ORDER:             'text-neon-green  bg-neon-green/10  border-neon-green/25',
  CAMPAIGN_TYPE_DISCOUNT:          'text-neon-red    bg-neon-red/10    border-neon-red/25',
  CAMPAIGN_TYPE_APP_REVIEW:        'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/25',
  CAMPAIGN_TYPE_EXCHANGE:          'text-neon-pink   bg-neon-pink/10   border-neon-pink/25'
}

// Active pill classes mirror the badge colors (border uses /30 for pill vs /25 for badge)
export const DEAL_TYPE_ACTIVE_CLASSES: Record<CampaignType, string> = {
  CAMPAIGN_TYPE_RATING:            'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/30',
  CAMPAIGN_TYPE_REVIEW:            'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/30',
  CAMPAIGN_TYPE_ORDER:             'text-neon-green  bg-neon-green/10  border-neon-green/30',
  CAMPAIGN_TYPE_DISCOUNT:          'text-neon-red    bg-neon-red/10    border-neon-red/30',
  CAMPAIGN_TYPE_APP_REVIEW:        'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/30',
  CAMPAIGN_TYPE_EXCHANGE:          'text-neon-pink   bg-neon-pink/10   border-neon-pink/30'
}

// Badge colors for required-screenshot step tags, mirroring the color assignments in
// constants/claimSteps.ts STEP_TYPE_COLORS (used by the claim stepper) for visual consistency.
export const REQUIRED_STEP_COLORS: Record<string, string> = {
  ORDER:            'text-neon-blue   bg-neon-blue/10   border-neon-blue/25',
  DELIVERY:         'text-neon-pink   bg-neon-pink/10   border-neon-pink/25',
  RATING:           'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
  REVIEW:           'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/25',
  SELLER_FEEDBACK:  'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/25',
  RETURN_WINDOW:    'text-neon-orange bg-neon-orange/10 border-neon-orange/25',
  DOWNLOAD_INSTALL: 'text-neon-plum   bg-neon-plum/10   border-neon-plum/25',
}

export const ALL_TYPES_OPTION: FilterOption<DealTypeFilter> = { value: 'all', label: 'All Types' }
export const ALL_PLATFORMS_OPTION: FilterOption<DealPlatformFilter> = { value: 'all', label: 'All Platforms' }
