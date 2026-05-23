import { useState } from 'react'
import { publishAssignment } from '../../../api/assignmentApi'
import { Toast } from '../Toast'

function paise(amount: number) {
  return `₹${(amount / 100).toLocaleString('en-IN')}`
}

export interface AssignmentFormFields {
  yourCommissionPaise: number
}

interface AssignmentFormProps {
  assignmentId:              string
  campaignId:                string
  basePricePaise:            number
  commissionOfferedPaise:    number
  slotsOffered:              number
  readOnly?:                 boolean
  commissionChargedPaise?:   number
  onPublished?: () => void
}

export function AssignmentForm({
  assignmentId,
  campaignId,
  basePricePaise,
  commissionOfferedPaise,
  slotsOffered,
  readOnly = false,
  commissionChargedPaise,
  onPublished,
}: AssignmentFormProps) {
  const [yourCommission, setYourCommission] = useState('')
  const [loading, setLoading]   = useState(false)
  const [success, setSuccess]   = useState(false)
  const [error, setError]       = useState<string | null>(null)

  const editedCommissionPaise = Math.round((parseFloat(yourCommission) || 0) * 100)
  const yourCommissionPaise   = readOnly ? (commissionChargedPaise ?? 0) : editedCommissionPaise
  const offeredPricePaise     = basePricePaise + yourCommissionPaise
  const netEarningsPaise      = commissionOfferedPaise + yourCommissionPaise

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await publishAssignment(assignmentId, campaignId, yourCommissionPaise, offeredPricePaise)
      setSuccess(true)
      onPublished?.()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to publish assignment.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      {success && (
        <Toast
          type="success"
          message="Assignment published successfully!"
          onDismiss={() => setSuccess(false)}
        />
      )}
      {error && (
        <Toast
          type="error"
          message={error}
          onDismiss={() => setError(null)}
        />
      )}

      <div className="rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-6 flex flex-col gap-6">
        <div className="flex items-center justify-between">
          <h3 className="text-base font-bold text-ink-light-primary dark:text-ink-dark-primary">
            {readOnly ? 'Assignment Details' : 'Configure & Publish'}
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
            {readOnly ? (
              <ReadOnlyField label="Your Commission" value={paise(yourCommissionPaise)} />
            ) : (
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
            )}
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

          {!readOnly && (
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 rounded-lg bg-neon-purple text-surface-dark-base text-sm font-semibold hover:brightness-110 transition-all mt-2 disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loading ? (
                <>
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v1m0 16v1m8.66-13H20m-16 0H2.34M17.66 17.66l-.71-.71M7.05 7.05l-.71-.71M17.66 6.34l-.71.71M7.05 16.95l-.71.71" />
                  </svg>
                  Publishing…
                </>
              ) : (
                'Publish'
              )}
            </button>
          )}
        </form>
      </div>
    </>
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
