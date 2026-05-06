import type { Platform, CampaignType, DealTypeFilter, DealPlatformFilter } from '../types/DealTypes'
import type { FilterOption } from '../components/ui/StatusFilterPills'

export const PLATFORM_COLORS: Record<Platform, string> = {
  PLATFORM_AMAZON:   'text-neon-orange bg-neon-orange/10 border-neon-orange/25',
  PLATFORM_FLIPKART: 'text-neon-blue   bg-neon-blue/10   border-neon-blue/25',
  PLATFORM_NYKAA:    'text-neon-pink   bg-neon-pink/10   border-neon-pink/25',
  PLATFORM_MYNTRA:   'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
}

export const DEAL_TYPE_COLORS: Record<CampaignType, string> = {
  CAMPAIGN_TYPE_RATING:            'text-neon-yellow bg-neon-yellow/10 border-neon-yellow/25',
  CAMPAIGN_TYPE_REVIEW:            'text-neon-cyan   bg-neon-cyan/10   border-neon-cyan/25',
  CAMPAIGN_TYPE_ORDER:             'text-neon-green  bg-neon-green/10  border-neon-green/25',
  CAMPAIGN_TYPE_DISCOUNT:          'text-neon-red    bg-neon-red/10    border-neon-red/25',
  CAMPAIGN_TYPE_AGENCY_DISCRETION: 'text-neon-purple bg-neon-purple/10 border-neon-purple/25',
}

export const ALL_TYPES_OPTION: FilterOption<DealTypeFilter> = { value: 'all', label: 'All Types', activeClasses: 'bg-neon-blue/10 text-neon-blue border-neon-blue/30' }
export const ALL_PLATFORMS_OPTION: FilterOption<DealPlatformFilter> = { value: 'all', label: 'All Platforms' }
