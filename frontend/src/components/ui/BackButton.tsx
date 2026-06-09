import { IconChevronLeft } from './icons'

interface Props {
  onClick: () => void
}

export function BackButton({ onClick }: Props) {
  return (
    <button
      onClick={onClick}
      className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
      aria-label="Back"
    >
      <IconChevronLeft size={16} />
    </button>
  )
}