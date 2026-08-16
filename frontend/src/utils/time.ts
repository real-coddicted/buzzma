export function yyyymmddToIso(n: number | undefined): string {
  if (!n) return ''
  const s = n.toString().padStart(8, '0')
  return `${s.slice(0, 4)}-${s.slice(4, 6)}-${s.slice(6, 8)}`
}

export function toRelativeTime(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(diff / 60_000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}d ago`
  return formatShortDate(iso)
}

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

const SHORT_MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

/** Formats a 'YYYY-MM-DD' date (or a full ISO date-time) as 'Mon D, YYYY'. */
export function formatShortDate(iso: string | null): string {
  if (!iso) return 'TBD'
  const parts = iso.split('T')[0].split('-')
  if (parts.length !== 3) return iso
  const [y, m, day] = parts
  return `${SHORT_MONTHS[parseInt(m, 10) - 1]} ${parseInt(day, 10)}, ${y}`
}