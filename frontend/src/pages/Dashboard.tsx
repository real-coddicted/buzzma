import { Card, CardHeader } from '../components/ui/Card'
import { StatCard } from '../components/ui/StatCard'
import { Button } from '../components/ui/Button'
import { StatusBadge } from '../components/ui/Badge'
import { IconPlus, IconChevronRight } from '../components/ui/icons'
import { statCards, recentActivity, campaigns, performanceBars } from '../data/mockData'
import type { StatCardAccent, ActivityItem, PerformanceBar } from '../types'

const accentDot: Record<StatCardAccent, string> = {
  red:    'bg-neon-red',
  orange: 'bg-neon-orange',
  yellow: 'bg-neon-yellow',
  green:  'bg-neon-green',
  cyan:   'bg-neon-cyan',
  blue:   'bg-neon-blue',
  purple: 'bg-neon-purple',
  pink:   'bg-neon-pink',
}

const accentText: Record<StatCardAccent, string> = {
  red:    'text-neon-red',
  orange: 'text-neon-orange',
  yellow: 'text-neon-yellow',
  green:  'text-neon-green',
  cyan:   'text-neon-cyan',
  blue:   'text-neon-blue',
  purple: 'text-neon-purple',
  pink:   'text-neon-pink',
}

const accentBar: Record<StatCardAccent, string> = {
  red:    'bg-neon-red shadow-neon-red',
  orange: 'bg-neon-orange shadow-neon-orange',
  yellow: 'bg-neon-yellow',
  green:  'bg-neon-green shadow-neon-green',
  cyan:   'bg-neon-cyan shadow-neon-cyan',
  blue:   'bg-neon-blue shadow-neon-blue',
  purple: 'bg-neon-purple shadow-neon-purple',
  pink:   'bg-neon-pink',
}

function ActivityRow({ item }: { item: ActivityItem }) {
  return (
    <div className="flex items-start gap-3 py-3 first:pt-0 last:pb-0">
      <div className={['mt-1.5 w-2 h-2 rounded-full flex-shrink-0', accentDot[item.accent]].join(' ')} />
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap">
          <span className={['text-xs font-semibold', accentText[item.accent]].join(' ')}>
            {item.message}
          </span>
          <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
            {item.timestamp}
          </span>
        </div>
        <p className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5 truncate">
          {item.detail}
        </p>
      </div>
    </div>
  )
}

function PerformanceBarRow({ bar }: { bar: PerformanceBar }) {
  return (
    <div className="flex items-center gap-3">
      <span className="text-xs font-medium text-ink-light-secondary dark:text-ink-dark-secondary w-14 flex-shrink-0">
        {bar.label}
      </span>
      <div className="flex-1 h-2 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden">
        <div
          className={['h-full rounded-full transition-all duration-700', accentBar[bar.accent]].join(' ')}
          style={{ width: `${(bar.value / bar.max) * 100}%` }}
        />
      </div>
      <span className={['text-xs font-semibold tabular-nums w-8 text-right', accentText[bar.accent]].join(' ')}>
        {bar.value}%
      </span>
    </div>
  )
}

export function Dashboard() {
  const topCampaigns = campaigns.filter(c => c.status === 'active').slice(0, 3)

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Good morning, Alex
          </h1>
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            Here's what's happening with your campaigns today.
          </p>
        </div>
        <Button variant="primary" leftIcon={<IconPlus size={14} />}>
          New Campaign
        </Button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {statCards.map(card => (
          <StatCard key={card.id} data={card} />
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <Card className="lg:col-span-2" padded={false}>
          <div className="p-5 pb-0">
            <CardHeader
              title="Channel Performance"
              subtitle="Score index by engagement quality"
              action={
                <Button variant="ghost" size="sm" rightIcon={<IconChevronRight size={12} />}>
                  Details
                </Button>
              }
            />
          </div>
          <div className="px-5 pb-5 space-y-4">
            {performanceBars.map(bar => (
              <PerformanceBarRow key={bar.label} bar={bar} />
            ))}
          </div>
          <div className="px-5 pb-5">
            <div className="rounded-lg border border-surface-light-border dark:border-surface-dark-border p-4 bg-surface-light-hover dark:bg-surface-dark-hover">
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                  Weekly impressions
                </span>
                <span className="text-xs text-neon-green font-semibold">+18.4%</span>
              </div>
              <div className="flex items-end gap-1.5 h-16">
                {[42, 58, 35, 71, 63, 88, 76].map((h, i) => (
                  <div key={i} className="flex-1 flex flex-col justify-end">
                    <div
                      className="rounded-sm w-full transition-all duration-300"
                      style={{
                        height: `${h}%`,
                        background: i === 6
                          ? 'linear-gradient(180deg, #57c7ff 0%, #bd93f9 100%)'
                          : undefined,
                      }}
                      data-day={i}
                    >
                      {i !== 6 && (
                        <div className="w-full h-full rounded-sm bg-surface-light-border dark:bg-surface-dark-border" />
                      )}
                    </div>
                  </div>
                ))}
              </div>
              <div className="flex justify-between mt-1.5">
                {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(d => (
                  <span key={d} className="flex-1 text-center text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
                    {d}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </Card>

        <Card padded={false}>
          <div className="p-5">
            <CardHeader
              title="Recent Activity"
              subtitle="Last 6 events"
              action={
                <Button variant="ghost" size="sm" rightIcon={<IconChevronRight size={12} />}>
                  All
                </Button>
              }
            />
          </div>
          <div className="px-5 pb-5 divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {recentActivity.map(item => (
              <ActivityRow key={item.id} item={item} />
            ))}
          </div>
        </Card>
      </div>

      <Card padded={false}>
        <div className="p-5">
          <CardHeader
            title="Active Campaigns"
            subtitle={`${topCampaigns.length} campaigns running`}
            action={
              <Button variant="ghost" size="sm" rightIcon={<IconChevronRight size={12} />}>
                View all
              </Button>
            }
          />
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="border-t border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
                {['Campaign', 'Status', 'Budget', 'Spent', 'Impressions', 'CTR', 'Conversions'].map(h => (
                  <th
                    key={h}
                    className="text-left px-5 py-2.5 font-semibold text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider text-[10px]"
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
              {topCampaigns.map(c => (
                <tr
                  key={c.id}
                  className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
                >
                  <td className="px-5 py-3.5 font-medium text-ink-light-primary dark:text-ink-dark-primary">
                    {c.name}
                  </td>
                  <td className="px-5 py-3.5">
                    <StatusBadge status={c.status} />
                  </td>
                  <td className="px-5 py-3.5 font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
                    ${c.budget.toLocaleString()}
                  </td>
                  <td className="px-5 py-3.5">
                    <div>
                      <span className="font-mono text-ink-light-primary dark:text-ink-dark-primary">
                        ${c.spent.toLocaleString()}
                      </span>
                      <div className="mt-1 h-1 rounded-full bg-surface-light-hover dark:bg-surface-dark-hover overflow-hidden w-20">
                        <div
                          className="h-full rounded-full bg-neon-blue"
                          style={{ width: `${Math.round((c.spent / c.budget) * 100)}%` }}
                        />
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-3.5 font-mono text-ink-light-secondary dark:text-ink-dark-secondary">
                    {c.impressions >= 1000000
                      ? `${(c.impressions / 1000000).toFixed(1)}M`
                      : `${(c.impressions / 1000).toFixed(0)}K`}
                  </td>
                  <td className="px-5 py-3.5 font-mono font-semibold text-neon-cyan">
                    {c.ctr.toFixed(2)}%
                  </td>
                  <td className="px-5 py-3.5 font-mono font-semibold text-neon-green">
                    {c.conversions.toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  )
}
