import type { HTMLAttributes, ReactNode } from 'react'

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode
  padded?: boolean
  hoverable?: boolean
}

export function Card({
  children,
  padded = true,
  hoverable = false,
  className = '',
  ...props
}: CardProps) {
  return (
    <div
      className={[
        'rounded-xl border bg-surface-light-card dark:bg-surface-dark-card',
        'border-surface-light-border dark:border-surface-dark-border',
        'shadow-card-light dark:shadow-card-dark',
        padded ? 'p-5' : '',
        hoverable
          ? 'transition-all duration-150 cursor-pointer hover:border-neon-blue/40 dark:hover:border-neon-blue/40 hover:-translate-y-0.5'
          : '',
        className,
      ].join(' ')}
      {...props}
    >
      {children}
    </div>
  )
}

interface CardHeaderProps {
  title: string
  subtitle?: string
  action?: ReactNode
}

export function CardHeader({ title, subtitle, action }: CardHeaderProps) {
  return (
    <div className="flex items-start justify-between mb-4">
      <div>
        <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          {title}
        </h3>
        {subtitle && (
          <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            {subtitle}
          </p>
        )}
      </div>
      {action && <div className="ml-4 flex-shrink-0">{action}</div>}
    </div>
  )
}
