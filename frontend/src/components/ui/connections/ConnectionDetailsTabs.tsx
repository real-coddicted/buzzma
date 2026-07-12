import { Tabs } from '../Tabs'

export type ConnectionDetailsTab = 'profile' | 'activity'

const tabs = [
  { value: 'profile' as const,  label: 'Profile'  },
  { value: 'activity' as const, label: 'Activity' },
]

interface ConnectionDetailsTabsProps {
  value: ConnectionDetailsTab
  onChange: (tab: ConnectionDetailsTab) => void
}

export function ConnectionDetailsTabs({ value, onChange }: ConnectionDetailsTabsProps) {
  return <Tabs options={tabs} value={value} onChange={onChange} />
}
