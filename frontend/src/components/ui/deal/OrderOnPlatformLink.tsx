import { IconExternalLink } from '../icons'

interface OrderOnPlatformLinkProps {
  productUrl: string
  platformLabel: string
  disabled?: boolean
}

export function OrderOnPlatformLink({ productUrl, platformLabel, disabled = false }: OrderOnPlatformLinkProps) {
  if (disabled) {
    return (
      <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-ink-light-muted dark:text-ink-dark-muted cursor-not-allowed">
        Order on {platformLabel}
        <IconExternalLink size={12} strokeWidth={2.5} />
      </span>
    )
  }

  return (
    <a
      href={productUrl}
      target="_blank"
      rel="noopener noreferrer"
      onClick={e => e.stopPropagation()}
      className="inline-flex items-center gap-1.5 text-xs font-semibold text-neon-cyan hover:text-neon-blue transition-colors"
    >
      Order on {platformLabel}
      <IconExternalLink size={12} strokeWidth={2.5} />
    </a>
  )
}