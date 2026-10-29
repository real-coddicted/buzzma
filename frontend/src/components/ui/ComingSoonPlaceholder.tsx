import { ImageFiller } from './ImageFiller'

interface ComingSoonPlaceholderProps {
  variant: 'image' | 'text'
}

/** Shared "content not ready yet" placeholder, used anywhere a page has a slot for copy or a screenshot that hasn't been authored/attached yet. */
export function ComingSoonPlaceholder({ variant }: ComingSoonPlaceholderProps) {
  if (variant === 'image') {
    return (
      <div className="w-full aspect-video max-w-sm rounded-lg border border-dashed border-surface-light-border dark:border-surface-dark-border flex flex-col items-center justify-center gap-2 bg-surface-light-hover dark:bg-surface-dark-hover">
        <ImageFiller />
        <span className="text-[10px] font-semibold uppercase tracking-wider text-neon-yellow">Coming soon</span>
      </div>
    )
  }

  return (
    <span className="text-[10px] font-semibold uppercase tracking-wider text-neon-yellow">Coming soon</span>
  )
}
