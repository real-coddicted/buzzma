import type { CampaignStatus, Platform, CampaignType } from '../../../../types'

export interface ChipColors {
  /** Always-visible color — matches Badge.tsx variantClasses (bg-X/10 text-X border-X/25) */
  base: string
  /** Stronger variant for the selected/active state in filter pills */
  selected: string
}

export const PLATFORM_COLORS: Record<Platform, ChipColors> = {
  PLATFORM_AMAZON:   { base: 'bg-neon-orange/10 text-neon-orange border-neon-orange/25', selected: 'bg-neon-orange/20 text-neon-orange border-neon-orange/50' },
  PLATFORM_FLIPKART: { base: 'bg-neon-blue/10   text-neon-blue   border-neon-blue/25',   selected: 'bg-neon-blue/20   text-neon-blue   border-neon-blue/50' },
  PLATFORM_NYKAA:    { base: 'bg-neon-pink/10   text-neon-pink   border-neon-pink/25',   selected: 'bg-neon-pink/20   text-neon-pink   border-neon-pink/50' },
  PLATFORM_MYNTRA:   { base: 'bg-neon-purple/10 text-neon-purple border-neon-purple/25', selected: 'bg-neon-purple/20 text-neon-purple border-neon-purple/50' },
}

export const TYPE_COLORS: Record<CampaignType, ChipColors> = {
  CAMPAIGN_TYPE_RATING:   { base: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/25', selected: 'bg-neon-yellow/20 text-neon-yellow border-neon-yellow/50' },
  CAMPAIGN_TYPE_REVIEW:   { base: 'bg-neon-cyan/10   text-neon-cyan   border-neon-cyan/25',   selected: 'bg-neon-cyan/20   text-neon-cyan   border-neon-cyan/50' },
  CAMPAIGN_TYPE_ORDER:    { base: 'bg-neon-green/10  text-neon-green  border-neon-green/25',  selected: 'bg-neon-green/20  text-neon-green  border-neon-green/50' },
  CAMPAIGN_TYPE_DISCOUNT: { base: 'bg-neon-red/10    text-neon-red    border-neon-red/25',    selected: 'bg-neon-red/20    text-neon-red    border-neon-red/50' },
}

export const STATUS_COLORS: Record<CampaignStatus, ChipColors> = {
  active:    { base: 'bg-neon-green/10  text-neon-green  border-neon-green/25',  selected: 'bg-neon-green/20  text-neon-green  border-neon-green/50' },
  paused:    { base: 'bg-neon-yellow/10 text-neon-yellow border-neon-yellow/25', selected: 'bg-neon-yellow/20 text-neon-yellow border-neon-yellow/50' },
  completed: { base: 'bg-neon-cyan/10   text-neon-cyan   border-neon-cyan/25',   selected: 'bg-neon-cyan/20   text-neon-cyan   border-neon-cyan/50' },
  closed:    { base: 'bg-neon-red/10    text-neon-red    border-neon-red/25',    selected: 'bg-neon-red/20    text-neon-red    border-neon-red/50' },
  draft:     {
    base:     'bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-secondary dark:text-ink-dark-secondary border-surface-light-border dark:border-surface-dark-border',
    selected: 'bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary   dark:text-ink-dark-primary   border-surface-light-border dark:border-surface-dark-border',
  },
}
