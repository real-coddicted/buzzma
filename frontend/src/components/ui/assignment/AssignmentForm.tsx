import { useState } from 'react'

function paise(amount: number) {
  return `₹${(amount / 100).toLocaleString('en-IN')}`
}

export interface AssignmentFormFields {
  yourCommissionPaise: number
}

interface AssignmentFormProps {
  basePricePaise:         number
  commissionOfferedPaise: number
  slotsOffered:           number
  onSubmit: (fields: AssignmentFormFields) => void
}

export function AssignmentForm({ basePricePaise, commissionOfferedPaise, slotsOffered, onSubmit }: AssignmentFormProps) {
  const [yourCommission, setYourCommission] = useState('')

  const yourCommissionPaise = Math.round((parseFloat(yourCommission) || 0) * 100)
  const offeredPricePaise   = basePricePaise + yourCommissionPaise
  const netEarningsPaise    = commissionOfferedPaise + yourCommissionPaise

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSubmit({ yourCommissionPaise })
  }

  return (
    <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-6 flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h3 className="text-base font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Configure & Publish
        </h3>
        <span className="text-[11px] font-semibold px-2.5 py-1 rounded-full bg-neon-green/10 text-neon-green border border-neon-green/25">
          {slotsOffered} {slotsOffered === 1 ? 'slot' : 'slots'}
        </span>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {/* Row 1 */}
        <div className="grid grid-cols-2 gap-4">
          <ReadOnlyField label="Commission Offered" value={paise(commissionOfferedPaise)} />
          <ReadOnlyField label="Base Price"         value={paise(basePricePaise)} />
        </div>

        {/* Row 2 */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
              Your Commission
            </label>
            <input
              type="number"
              placeholder="0.00"
              value={yourCommission}
              onChange={e => setYourCommission(e.target.value)}
              className="w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card text-ink-light-primary dark:text-ink-dark-primary px-3 py-2 outline-none focus:border-neon-blue/50 transition-colors placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
            />
            <p className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted mt-1">
              Positive or negative values allowed
            </p>
          </div>
          <ReadOnlyField
            label="Offered Price"
            value={paise(offeredPricePaise)}
            hint="Base price + your commission"
          />
        </div>

        {/* Row 3 — full width net earnings */}
        <ReadOnlyField
          label="Your Net Earnings"
          value={paise(netEarningsPaise)}
          hint="Commission offered + your commission"
          accent={netEarningsPaise >= 0 ? 'green' : 'red'}
        />

        <button
          type="submit"
          className="w-full py-2.5 rounded-lg bg-neon-purple text-surface-dark-base text-sm font-semibold hover:brightness-110 transition-all mt-2"
        >
          Publish
        </button>
      </form>
    </div>
  )
}

function ReadOnlyField({
  label,
  value,
  hint,
  accent,
}: {
  label:   string
  value:   string
  hint?:   string
  accent?: 'green' | 'red'
}) {
  const valueClass =
    accent === 'green' ? 'text-neon-green' :
    accent === 'red'   ? 'text-neon-red'   :
    'text-ink-light-primary dark:text-ink-dark-primary'

  return (
    <div>
      <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
        {label}
      </label>
      <div className="w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover px-3 py-2 select-none">
        <span className={['font-semibold', valueClass].join(' ')}>{value}</span>
      </div>
      {hint && (
        <p className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted mt-1">{hint}</p>
      )}
    </div>
  )
}
