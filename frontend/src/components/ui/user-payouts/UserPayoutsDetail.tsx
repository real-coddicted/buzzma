import { Card } from '../Card'
import { Badge } from '../Badge'
import { Button } from '../Button'
import { formatRupees } from '../../../utils/currency'
import type { PayoutUser, PayoutClaim } from '../../../types/UserPayoutsTypes'

interface Props {
  user: PayoutUser
  claims: PayoutClaim[]
  selectedClaims: Set<string>
  onToggleClaim: (id: string) => void
  onToggleAll: (checked: boolean) => void
  onPayAll: () => void
  onPaySelected: () => void
}

export function UserPayoutsDetail({
  user,
  claims,
  selectedClaims,
  onToggleClaim,
  onToggleAll,
  onPayAll,
  onPaySelected,
}: Props) {
  const total     = claims.reduce((s, c) => s + c.amount, 0)
  const allChecked = claims.length > 0 && claims.every(c => selectedClaims.has(c.id))
  const selectedTotal = [...selectedClaims].reduce((s, id) => {
    const c = claims.find(x => x.id === id)
    return s + (c ? c.amount : 0)
  }, 0)

  return (
    <div className="flex flex-col gap-4">
      {/* User header */}
      <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-4 flex items-center gap-4">
        <div className="w-12 h-12 rounded-full bg-neon-blue/10 flex items-center justify-center text-base font-bold text-neon-blue flex-shrink-0">
          {user.initials}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-base font-semibold text-ink-light-primary dark:text-ink-dark-primary">{user.name}</p>
          <div className="flex items-center gap-2 mt-1">
            <Badge variant={user.role === 'Mediator' ? 'blue' : 'green'} textClass="text-xs font-medium">
              {user.role}
            </Badge>
            <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">UPI: {user.upiId}</span>
          </div>
        </div>
        <div className="text-right flex-shrink-0 mr-3">
          <p className="text-xl font-bold text-neon-orange">₹{formatRupees(total)}</p>
          <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted">{claims.length} claims pending</p>
        </div>
        <Button variant="primary" onClick={onPayAll}>
          Pay All (₹{formatRupees(total)})
        </Button>
      </div>

      {/* Selection context bar */}
      {selectedClaims.size > 0 && (
        <div className="flex items-center justify-between px-4 py-2.5 rounded-xl bg-neon-blue/5 border border-neon-blue/20">
          <span className="text-sm text-neon-blue font-medium">
            {selectedClaims.size} claim{selectedClaims.size > 1 ? 's' : ''} selected &nbsp;·&nbsp;
            Total: <strong>₹{formatRupees(selectedTotal)}</strong>
          </span>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onToggleAll(false)}
          >
            Clear
          </Button>
        </div>
      )}

      {/* Claims list */}
      <Card padded={false}>
        {/* Select-all bar */}
        <div className="flex items-center justify-between px-4 py-2.5 border-b border-surface-light-border dark:border-surface-dark-border">
          <label className="flex items-center gap-2.5 cursor-pointer">
            <input
              type="checkbox"
              checked={allChecked}
              onChange={e => onToggleAll(e.target.checked)}
              className="w-4 h-4 accent-neon-blue cursor-pointer"
            />
            <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">Select all claims</span>
            {selectedClaims.size > 0 && (
              <span className="px-2 py-0.5 rounded-full bg-neon-blue/10 text-neon-blue text-[10px] font-semibold border border-neon-blue/20">
                {selectedClaims.size} selected
              </span>
            )}
          </label>
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
            {claims.length} pending claim{claims.length !== 1 ? 's' : ''} · Click to select for partial payment
          </span>
        </div>

        {/* Claim rows */}
        <ul>
          {claims.map((claim, i) => {
            const checked = selectedClaims.has(claim.id)
            const isLast  = i === claims.length - 1
            return (
              <li
                key={claim.id}
                className={[
                  'flex items-center gap-3 px-4 py-3 cursor-pointer transition-colors',
                  checked
                    ? 'bg-neon-blue/5 hover:bg-neon-blue/8'
                    : 'hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
                  !isLast ? 'border-b border-surface-light-border dark:border-surface-dark-border' : '',
                ].join(' ')}
                onClick={() => onToggleClaim(claim.id)}
              >
                <input
                  type="checkbox"
                  checked={checked}
                  onChange={() => onToggleClaim(claim.id)}
                  onClick={e => e.stopPropagation()}
                  className="w-4 h-4 accent-neon-blue cursor-pointer flex-shrink-0"
                />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">{claim.campaign}</p>
                  <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
                    {claim.brand}
                    <span className="mx-1.5">·</span>
                    <span className="font-mono">{claim.id}</span>
                  </p>
                </div>
                <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted flex-shrink-0">
                  {claim.approvedDate}
                </span>
                <span className="text-sm font-semibold text-neon-orange flex-shrink-0">
                  ₹{formatRupees(claim.amount)}
                </span>
              </li>
            )
          })}
        </ul>

        {/* Total footer */}
        <div className="flex items-center justify-end gap-3 px-4 py-2.5 border-t border-surface-light-border dark:border-surface-dark-border">
          <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">Total outstanding</span>
          <span className="text-base font-bold text-neon-orange">₹{formatRupees(total)}</span>
        </div>
      </Card>

      {/* Floating Pay Selected button */}
      {selectedClaims.size > 0 && (
        <button
          onClick={onPaySelected}
          className="fixed bottom-7 right-8 flex items-center gap-2.5 px-5 py-3 rounded-xl bg-neon-blue text-surface-dark-base font-semibold text-sm shadow-neon-blue/40 shadow-lg hover:brightness-110 transition-all z-50 animate-fade-in"
        >
          <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M2 8h12M9 4l5 4-5 4" />
          </svg>
          Pay Selected
          <span className="bg-white/25 rounded-full px-2 py-0.5 text-xs font-bold">
            ₹{formatRupees(selectedTotal)}
          </span>
        </button>
      )}
    </div>
  )
}
