import { Badge } from '../Badge'
import { USER_STATUS_CONFIG } from './userConstants'
import type { User } from '../../../types'

interface UserListItemProps {
  user: User
}

export function UserListItem({ user }: UserListItemProps) {
  const cfg = USER_STATUS_CONFIG[user.status]
  return (
    <li className="px-4 py-2.5 flex items-center justify-between gap-3 text-sm text-ink-light-primary dark:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
      <span className="truncate">{user.name}</span>
      <Badge variant={cfg.variant}>
        <span className={['w-1.5 h-1.5 rounded-full inline-block', cfg.dot].join(' ')} />
        {cfg.label}
      </Badge>
    </li>
  )
}
