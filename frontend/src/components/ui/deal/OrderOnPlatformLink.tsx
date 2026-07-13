import { IconExternalLink } from '../icons'

interface OrderOnPlatformLinkProps {
  productUrl: string
  platformLabel: string
}

export function OrderOnPlatformLink({ productUrl, platformLabel }: OrderOnPlatformLinkProps) {
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