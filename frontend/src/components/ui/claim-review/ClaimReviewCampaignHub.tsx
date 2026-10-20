import { useMemo, useState } from 'react'
import type { CampaignNameOption } from '../../../api/campaignApi'
import { IconSearch, IconCheck } from '../icons'

interface Props {
  campaigns: CampaignNameOption[]
  selectedCampaignId: string | null
  onSelect: (campaignId: string | null) => void
}

function labelFor(c: CampaignNameOption): string {
  return c.code ? `${c.title} (${c.code})` : c.title
}

/**
 * Campaign-first navigation for the order review queue - a searchable single-select dropdown
 * rather than a chip row, so it stays usable as the campaign list grows (see ClaimReviewList for
 * why it carries no counts).
 */
export function ClaimReviewCampaignHub({ campaigns, selectedCampaignId, onSelect }: Props) {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)

  const selected = campaigns.find(c => c.id === selectedCampaignId) ?? null

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return campaigns
    return campaigns.filter(c => labelFor(c).toLowerCase().includes(q))
  }, [campaigns, query])

  function handleSelect(id: string | null) {
    onSelect(id)
    setQuery('')
    setOpen(false)
  }

  return (
    <div className="relative w-full max-w-sm">
      <div className="relative">
        <IconSearch size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink-light-muted dark:text-ink-dark-muted pointer-events-none" />
        <input
          type="text"
          value={open ? query : (selected ? labelFor(selected) : '')}
          onChange={e => { setQuery(e.target.value); setOpen(true) }}
          onFocus={() => { setQuery(''); setOpen(true) }}
          onBlur={() => setTimeout(() => setOpen(false), 150)}
          placeholder="All campaigns"
          className="w-full bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border rounded-lg pl-8 pr-3 py-2 text-xs text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted outline-none focus:border-neon-blue/50 transition-colors"
        />
      </div>
      {open && (
        <div className="absolute left-0 right-0 z-10 mt-1 max-h-64 overflow-y-auto rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-lg">
          <button
            type="button"
            onMouseDown={e => e.preventDefault()}
            onClick={() => handleSelect(null)}
            className="w-full flex items-center justify-between gap-2 px-3 py-1.5 text-xs text-left font-semibold text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover"
          >
            All campaigns
            {selectedCampaignId === null && <IconCheck size={12} className="text-neon-blue flex-shrink-0" />}
          </button>
          {filtered.length === 0 ? (
            <div className="px-3 py-2 text-xs text-ink-light-muted dark:text-ink-dark-muted">No campaigns match &quot;{query}&quot;</div>
          ) : (
            filtered.map(c => (
              <button
                key={c.id}
                type="button"
                onMouseDown={e => e.preventDefault()}
                onClick={() => handleSelect(c.id)}
                className="w-full flex items-center justify-between gap-2 px-3 py-1.5 text-xs text-left text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover"
              >
                <span className="truncate">{labelFor(c)}</span>
                {selectedCampaignId === c.id && <IconCheck size={12} className="text-neon-blue flex-shrink-0" />}
              </button>
            ))
          )}
        </div>
      )}
    </div>
  )
}
