import { Avatar } from '../Avatar'
import { Badge } from '../Badge'
import { IconChevronRight } from '../icons'
import type { StatCardAccent } from '../../../types'
import type { UserSummaryDto } from '../../../api/userApi'

const ROLE_CONFIG: Record<string, { label: string; variant: StatCardAccent }> = {
  ROLE_BUYER:    { label: 'Buyer',    variant: 'blue' },
  ROLE_MEDIATOR: { label: 'Mediator', variant: 'purple' },
  ROLE_AGENCY:   { label: 'Agency',   variant: 'cyan' },
  ROLE_BRAND:    { label: 'Brand',    variant: 'orange' },
  ROLE_ADMIN:    { label: 'Admin',    variant: 'pink' },
}

const STATUS_CONFIG: Record<string, { label: string; variant: StatCardAccent; dot: string }> = {
  USER_STATUS_ACTIVE:    { label: 'Active',    variant: 'green',  dot: 'bg-neon-green animate-pulse-slow' },
  USER_STATUS_SUSPENDED: { label: 'Suspended', variant: 'yellow', dot: 'bg-neon-yellow' },
  USER_STATUS_LOCKED:    { label: 'Locked',    variant: 'red',    dot: 'bg-neon-red' },
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

interface UserListItemProps {
  user: UserSummaryDto
  onClick?: () => void
}

export function UserListItem({ user, onClick }: UserListItemProps) {
  const roleConfig   = user.role   ? ROLE_CONFIG[user.role]     : undefined
  const statusConfig = user.status ? STATUS_CONFIG[user.status] : undefined

  return (
    <li
      onClick={onClick}
      className={[
        'px-4 py-3 flex items-center gap-3 transition-colors',
        onClick ? 'cursor-pointer hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover group' : '',
      ].join(' ')}
    >
      <Avatar name={user.name} src={user.avatar} size="md" />

      <div className="flex-1 min-w-0">
      {/* Title + badges */}
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-1.5 min-w-0">
          <span className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary truncate">
            {user.name ?? '—'}
          </span>
          {roleConfig && (
            <Badge variant={roleConfig.variant}>{roleConfig.label}</Badge>
          )}
        </div>
        {statusConfig && (
          <Badge variant={statusConfig.variant}>
            <span className={['w-1.5 h-1.5 rounded-full inline-block', statusConfig.dot].join(' ')} />
            {statusConfig.label}
          </Badge>
        )}
      </div>

      {/* Subtitle — mobile */}
      <p className="mt-0.5 text-xs text-ink-light-secondary dark:text-ink-dark-secondary">
        {user.mobile ?? '—'}
      </p>

      {/* Detail row */}
      <div className="mt-2 flex items-center justify-between gap-3">
        {user.email && (
          <span className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
            {user.email}
          </span>
        )}
        {user.code && (
          <span className="font-mono text-[11px] text-neon-cyan bg-neon-cyan/10 border border-neon-cyan/25 rounded px-1.5 py-0.5">
            {user.code}
          </span>
        )}
        {user.createdAt && (
          <span className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted ml-auto">
            Joined on {formatDate(user.createdAt)}
          </span>
        )}
      </div>
      </div>

      {onClick && (
        <IconChevronRight
          size={14}
          className="text-ink-light-muted dark:text-ink-dark-muted group-hover:text-neon-blue transition-colors flex-shrink-0"
        />
      )}
    </li>
  )
}