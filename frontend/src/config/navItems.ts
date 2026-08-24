import type { ComponentType } from 'react'
import { IconDashboard, IconCampaign, IconUsers, IconBolt, IconFeedback, IconList, IconTicket, IconChart, IconProfile, IconRupee, IconPayouts, IconCheck } from '../components/ui/icons'
import type { NavPage } from '../types'
import type { components } from '../types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']
type IconComponent = ComponentType<{ size?: number }>

export interface NavItemConfig {
  page: NavPage
  label: string
  icon: IconComponent
  iconSize?: number
  flagKey: keyof UserSettingsDto
  section: 'main' | 'footer'
}

// Single source of truth for sidebar rendering order and the tab-redirect
// fallback order — keep both derived from this array so they can't drift apart.
export const NAV_ITEMS: NavItemConfig[] = [
  { page: 'dashboard', label: 'Dashboard', icon: IconDashboard, flagKey: 'dashboardTabEnabled', section: 'main' },
  { page: 'campaigns', label: 'Campaigns', icon: IconCampaign, flagKey: 'campaignsTabEnabled', section: 'main' },
  { page: 'assignments', label: 'Assigned Campaigns', icon: IconList, flagKey: 'assignmentsTabEnabled', section: 'main' },
  { page: 'deals', label: 'Deals', icon: IconBolt, flagKey: 'dealTabEnabled', section: 'main' },
  { page: 'my-claims', label: 'My Claims', icon: IconCheck, iconSize: 18, flagKey: 'myClaimsTabEnabled', section: 'main' },
  { page: 'claim-review', label: 'Claim Review', icon: IconChart, flagKey: 'claimReviewEnabled', section: 'main' },
  { page: 'my-payments', label: 'My Payments', icon: IconRupee, flagKey: 'myPaymentsTabEnabled', section: 'main' },
  { page: 'user-payouts', label: 'User Payouts', icon: IconPayouts, flagKey: 'userPayoutsTabEnabled', section: 'main' },
  { page: 'connections', label: 'My Network', icon: IconUsers, flagKey: 'connectionsTabEnabled', section: 'main' },
  { page: 'users', label: 'Users', icon: IconProfile, flagKey: 'usersTabEnabled', section: 'main' },
  { page: 'my-tickets', label: 'My Tickets', icon: IconTicket, flagKey: 'ticketsTabEnabled', section: 'footer' },
  { page: 'feedback', label: 'Feedback', icon: IconFeedback, flagKey: 'feedbackTabEnabled', section: 'footer' },
]
