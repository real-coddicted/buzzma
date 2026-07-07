import type { CampaignStatus } from '../types'

export type RawCampaignStatus =
  | 'CAMPAIGN_STATUS_DRAFT'
  | 'CAMPAIGN_STATUS_CLOSED'
  | 'CAMPAIGN_STATUS_ACTIVE'
  | 'CAMPAIGN_STATUS_ASSIGNED'
  | 'CAMPAIGN_STATUS_PAUSED'
  | 'CAMPAIGN_STATUS_COMPLETED'

export const campaignStatusMap: Record<RawCampaignStatus, CampaignStatus> = {
  CAMPAIGN_STATUS_DRAFT:     'draft',
  CAMPAIGN_STATUS_ACTIVE:    'active',
  CAMPAIGN_STATUS_ASSIGNED:  'active',
  CAMPAIGN_STATUS_PAUSED:    'paused',
  CAMPAIGN_STATUS_COMPLETED: 'completed',
  CAMPAIGN_STATUS_CLOSED:    'completed',
}
