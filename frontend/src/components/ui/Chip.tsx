import type { ReactNode } from 'react'

interface ChipProps {
  children: ReactNode
  tone?: 'dark' | 'light'
  className?: string
}

const toneClasses: Record<'dark' | 'light', string> = {
  dark:  'bg-ink-light-primary dark:bg-ink-dark-primary text-surface-light-card dark:text-surface-dark-card',
  light: 'bg-surface-light-card dark:bg-surface-dark-card text-ink-light-primary dark:text-ink-dark-primary',
}

export function Chip({ children, tone = 'light', className = '' }: ChipProps) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded shadow-sm ${toneClasses[tone]} ${className}`}>
      {children}
    </span>
  )
}
