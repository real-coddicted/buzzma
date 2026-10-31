import { IconEye } from './icons'

interface ViewAsBannerProps {
  name: string
  roleLabel?: string
  onExit: () => void
}

export function ViewAsBanner({ name, roleLabel, onExit }: ViewAsBannerProps) {
  return (
    <div
      role="status"
      className="flex items-center gap-2 px-3 sm:px-4 py-2 mb-4 rounded-lg border border-neon-orange/30 bg-neon-orange/10 text-xs text-neon-orange"
    >
      <IconEye size={14} className="flex-shrink-0" />
      <span className="flex-1 min-w-0 truncate">
        Viewing as <strong className="font-semibold">{name}</strong>
        {roleLabel && ` (${roleLabel})`} - read-only
      </span>
      <button
        type="button"
        onClick={onExit}
        className="flex-shrink-0 px-2 py-1 rounded-md font-semibold hover:bg-neon-orange/15 transition-colors"
      >
        Exit
      </button>
    </div>
  )
}
