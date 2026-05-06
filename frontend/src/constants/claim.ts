import type { ClaimStep, ClaimStatus } from '../types/DealTypes'
import type { CampaignType } from '../types/CampaignTypes'

export interface StepMeta {
  number:      ClaimStep
  label:       string
  description: string
}

export const CLAIM_STEPS: StepMeta[] = [
  { number: 1, label: 'Order Placed',   description: 'Order confirmed and submitted'         },
  { number: 2, label: 'Proof Upload',   description: 'Upload proof of action taken'          },
  { number: 3, label: 'Under Review',   description: 'Mediator is reviewing your submission' },
  { number: 4, label: 'Cash Disbursed', description: 'Cashback credited to your account'     },
]

export const CLAIM_STATUS_CONFIG: Record<ClaimStatus, { label: string; classes: string }> = {
  pending:  { label: 'Pending',  classes: 'text-neon-yellow border-neon-yellow/30 bg-neon-yellow/10' },
  approved: { label: 'Approved', classes: 'text-neon-green  border-neon-green/30  bg-neon-green/10'  },
  rejected: { label: 'Rejected', classes: 'text-neon-red    border-neon-red/30    bg-neon-red/10'    },
}

export const PROOF_UPLOAD_LABEL: Record<CampaignType, string | null> = {
  CAMPAIGN_TYPE_RATING:            'Upload a screenshot of your rating on the platform',
  CAMPAIGN_TYPE_REVIEW:            'Upload a screenshot of your written review',
  CAMPAIGN_TYPE_ORDER:             null,
  CAMPAIGN_TYPE_DISCOUNT:          'Upload proof of discount usage (screenshot or coupon)',
  CAMPAIGN_TYPE_AGENCY_DISCRETION: 'Upload relevant proof as instructed by the agency',
}
