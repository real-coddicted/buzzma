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
  return `${Math.floor(hours / 24)}d ago`
}

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

const SHORT_MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

/** Formats time remaining until a 'YYYY-MM-DD' end date as 'X days left' / 'Ends today', or null once past. */
export function formatDaysLeft(iso: string): string | null {
  const end = new Date(iso.split('T')[0] + 'T23:59:59')
  const days = Math.ceil((end.getTime() - Date.now()) / 86_400_000)
  if (days < 0) return null
  if (days === 0) return 'Ends today'
  return days === 1 ? '1 day left' : `${days} days left`
}

/** Formats a 'YYYY-MM-DD' date (or a full ISO date-time) as 'Mon D, YYYY'. */
export function formatShortDate(iso: string | null): string {
  if (!iso) return 'TBD'
  const parts = iso.split('T')[0].split('-')
  if (parts.length !== 3) return iso
  const [y, m, day] = parts
  return `${SHORT_MONTHS[parseInt(m, 10) - 1]} ${parseInt(day, 10)}, ${y}`
}