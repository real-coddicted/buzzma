export type CampaignStatus = 'active' | 'paused' | 'completed' | 'closed' | 'draft'

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

export type StatCardIcon =
  | 'megaphone'
  | 'users'
  | 'currency'
  | 'chart'
  | 'target'
  | 'bolt'

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
  code: string
  title: string
  status: CampaignStatus
  platform: Platform
  productBrandName: string
  productName: string
  productImageUrl: string
  productUrl: string
  originalPricePaise: number
  campaignPricePaise: number
  commissionOfferedPaise: number
  returnWindowDays: number | null
  campaignType: CampaignType | null
  totalSlots: number | null
  slotsClaimed: number
  allowedAgencies: string[] | null
  openToAll: boolean
  spent: number
  impressions: number
  clicks: number
  conversions: number
  ctr: number
  startDate: string
  endDate: string
}

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

export interface CampaignRequestDto {
  title: string
  platform: string
  productBrandName: string
  productName: string
  productImageUrl: string
  productUrl: string
  sellerName: string | null
  originalPricePaise: number
  campaignPricePaise: number
  commissionToAllPaise?: number
  returnWindowDays: number | null
  campaignType: CampaignType | null
  totalSlots: number | null
  assignees: LinkedEntity[] | null
  openToAll: boolean | null
  termsAndConditions: string | null
  startDate: string | null
  endDate: string | null
  action?: 'CAMPAIGN_ACTION_PUBLISH' | 'CAMPAIGN_ACTION_PAUSE' | 'CAMPAIGN_ACTION_RESUME' | 'CAMPAIGN_ACTION_CLOSE' | 'CAMPAIGN_ACTION_COMPLETE'
}

export interface PerformanceBar {
  label: string
  value: number
  max: number
  accent: StatCardAccent
}

export interface LinkedEntity {
  id: string
  name: string
  slotsAvailable: number
  commissionOffered: number
}
