export type Theme = 'light' | 'dark'

export type NavPage = 'dashboard' | 'campaigns' | 'feedback'

export type CampaignStatus = 'active' | 'paused' | 'completed' | 'draft'

export type TrendDirection = 'up' | 'down' | 'neutral'

export type StatCardAccent =
  | 'red'
  | 'orange'
  | 'yellow'
  | 'green'
  | 'cyan'
  | 'blue'
  | 'purple'
  | 'pink'

export interface StatCardData {
  id: string
  label: string
  value: string
  subValue?: string
  trend: TrendDirection
  trendValue: string
  accent: StatCardAccent
  icon: StatCardIcon
}

export type StatCardIcon =
  | 'megaphone'
  | 'users'
  | 'currency'
  | 'chart'
  | 'target'
  | 'bolt'

export interface ActivityItem {
  id: string
  type: 'campaign_launched' | 'campaign_paused' | 'goal_reached' | 'budget_alert' | 'new_user' | 'report_ready'
  message: string
  detail: string
  timestamp: string
  accent: StatCardAccent
}

export interface Campaign {
  id: string
  name: string
  status: CampaignStatus
  channel: CampaignChannel
  budget: number
  spent: number
  impressions: number
  clicks: number
  conversions: number
  ctr: number
  startDate: string
  endDate: string
}

export type CampaignChannel = 'email' | 'social' | 'search' | 'display' | 'video'

export type Platform =
  | 'PLATFORM_AMAZON'
  | 'PLATFORM_FLIPKART'
  | 'PLATFORM_NYKAA'
  | 'PLATFORM_MYNTRA'

export type CampaignType =
  | 'CAMPAIGN_TYPE_RATING'
  | 'CAMPAIGN_TYPE_REVIEW'
  | 'CAMPAIGN_TYPE_ORDER'
  | 'CAMPAIGN_TYPE_DISCOUNT'
  | 'CAMPAIGN_TYPE_AGENCY_DISCRETION'

export interface CampaignRequestDto {
  title: string
  platform: string
  productBrandName: string
  productImageUrl: string
  productUrl: string
  originalPricePaise: number
  campaignPricePaise: number
  commissionOfferedPaise: number
  returnWindowDays: number | null
  campaignType: CampaignType | null
  totalSlots: number | null
  allowedAgencies: string[] | null
  openToAll: boolean | null
}

export interface PerformanceBar {
  label: string
  value: number
  max: number
  accent: StatCardAccent
}
