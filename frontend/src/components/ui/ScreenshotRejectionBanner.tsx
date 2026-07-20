import { IconWarning } from './icons'

interface Props {
  comment: string
  label?: string
}

export function ScreenshotRejectionBanner({ comment, label }: Props) {
  const message = label
    ? `Your last ${label} screenshot was rejected for following reason:`
    : 'Rejected earlier for following reason:'

  return (
    <div className="w-full rounded-lg border border-surface-light-border dark:border-surface-dark-border px-4 py-3 space-y-1">
      <p className="flex items-center gap-1.5 text-xs text-ink-light-secondary dark:text-ink-dark-secondary">
        <IconWarning size={13} className="shrink-0 text-neon-orange" />
        {message}
      </p>
      <p className="text-xs font-semibold text-neon-red">{comment}</p>
    </div>
  )
}
