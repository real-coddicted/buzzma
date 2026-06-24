interface AvatarProps {
  name?: string
  src?: string
  size?: 'sm' | 'md' | 'lg'
}

const sizeClasses = {
  sm: 'w-7 h-7 text-[10px]',
  md: 'w-9 h-9 text-xs',
  lg: 'w-11 h-11 text-sm',
}

function initials(name: string) {
  const parts = name.trim().split(/\s+/)
  if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  return parts[0].slice(0, 2).toUpperCase()
}

export function Avatar({ name, src, size = 'md' }: AvatarProps) {
  const base = `${sizeClasses[size]} rounded-full flex-shrink-0 flex items-center justify-center font-semibold`

  if (src) {
    return (
      <img
        src={src}
        alt={name ?? ''}
        className={`${base} object-cover`}
      />
    )
  }

  return (
    <div className={`${base} bg-neon-blue/20 text-neon-blue`}>
      {name ? initials(name) : '?'}
    </div>
  )
}