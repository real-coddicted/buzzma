import { Card } from '../Card'
import { Badge } from '../Badge'
import { Button } from '../Button'
import { PaginationToolbar } from '../PaginationToolbar'
import { StatBanner } from '../my-payments/StatBanner'
import { IconChevronRight } from '../icons'
import { formatRupees } from '../../../utils/currency'
import type { PayoutUser } from '../../../types/UserPayoutsTypes'

interface Props {
  users: PayoutUser[]
  page: number
  onPageChange: (p: number) => void
  onOpenDetail: (user: PayoutUser) => void
  onPayUser: (user: PayoutUser) => void
}

const PAGE_SIZE = 5

export function UserPayoutsList({ users, page, onPageChange, onOpenDetail, onPayUser }: Props) {
  const activeUsers  = users.filter(u => u.claimCount > 0)
  const totalPending = activeUsers.reduce((s, u) => s + u.totalAmount, 0)
  const totalClaims  = activeUsers.reduce((s, u) => s + u.claimCount, 0)
  const totalPages   = Math.ceil(activeUsers.length / PAGE_SIZE)
  const paged        = activeUsers.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE)

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h1 className="text-xl font-semibold text-ink-light-primary dark:text-ink-dark-primary">User Payouts</h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          Manage pending payouts to your downward network
        </p>
      </div>

      <StatBanner
        label="Total Payout Pending"
        amount={totalPending}
        subtitle={`across ${activeUsers.length} user${activeUsers.length !== 1 ? 's' : ''}`}
        rightCount={totalClaims}
        rightLabel="Total Claims"
        accent="orange"
      />

      <Card padded={false}>
        <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border flex items-center justify-between">
          <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">Pending Payouts</span>
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
            Sorted by oldest claim first · Click row to view &amp; select claims
          </span>
        </div>

        {activeUsers.length === 0 ? (
          <p className="px-4 py-12 text-sm text-center text-ink-light-muted dark:text-ink-dark-muted">
            🎉 No pending payouts. All caught up!
          </p>
        ) : (
          <ul>
            {paged.map((user, i) => {
              const isLast = i === paged.length - 1
              return (
                <li
                  key={user.id}
                  className={[
                    'flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors',
                    !isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : '',
                  ].join(' ')}
                  onClick={() => onOpenDetail(user)}
                >
                  <span className="w-5 text-center text-xs text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">
                    {(page - 1) * PAGE_SIZE + i + 1}
                  </span>
                  <div className="w-9 h-9 rounded-full bg-neon-blue/10 flex items-center justify-center text-xs font-bold text-neon-blue flex-shrink-0">
                    {user.initials}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">{user.name}</p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <Badge variant={user.role === 'Mediator' ? 'blue' : 'green'} textClass="text-[10px] font-semibold">
                        {user.role}
                      </Badge>
                      <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
                        {user.claimCount} claim{user.claimCount !== 1 ? 's' : ''} pending
                      </span>
                    </div>
                  </div>
                  <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">
                    Oldest: {user.oldestClaimDate}
                  </span>
                  <span className="text-sm font-semibold text-neon-orange flex-shrink-0">
                    ₹{formatRupees(user.totalAmount)}
                  </span>
                  <div className="flex-shrink-0" onClick={e => { e.stopPropagation(); onPayUser(user) }}>
                    <Button variant="primary" size="sm">Pay User</Button>
                  </div>
                  <IconChevronRight className="text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0" />
                </li>
              )
            })}
          </ul>
        )}

        <PaginationToolbar currentPage={page} totalPages={totalPages} onPageChange={onPageChange} />
      </Card>
    </div>
  )
}
