import { useState } from 'react'
import { Card } from '../Card'
import { Button } from '../Button'
import { RupeeInput } from '../RupeeInput'
import { ScreenshotUpload } from '../deal/ScreenshotUpload'
import { formatRupees } from '../../../utils/currency'
import { PAYMENT_METHODS } from '../../../types/UserPayoutsTypes'
import type { PayoutClaim, PaymentSubmission } from '../../../types/UserPayoutsTypes'

const inputClass = [
  'w-full px-3 py-2 rounded-lg border text-sm',
  'border-surface-light-border dark:border-surface-dark-border',
  'bg-surface-light-raised dark:bg-surface-dark-raised',
  'text-ink-light-primary dark:text-ink-dark-primary',
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
  'focus:outline-none focus:ring-2 focus:ring-neon-blue/30 focus:border-neon-blue/50',
  'transition-colors',
].join(' ')

interface Props {
  userName: string
  claimsToPay: PayoutClaim[]
  onSubmit: (submission: PaymentSubmission) => Promise<void>
}

export function PaymentForm({ userName, claimsToPay, onSubmit }: Props) {
  const totalAmount = claimsToPay.reduce((s, c) => s + c.amount, 0)

  const [screenshot, setScreenshot]     = useState<File | null>(null)
  const [amountPaid, setAmountPaid]     = useState(String(totalAmount))
  const [method, setMethod]             = useState('')
  const [otherMethod, setOtherMethod]   = useState('')
  const [utrRef, setUtrRef]             = useState('')
  const [notes, setNotes]               = useState('')
  const [submitting, setSubmitting]     = useState(false)

  async function handleSubmit() {
    if (!screenshot) { alert('Please upload a payment screenshot.'); return }
    if (!amountPaid || Number(amountPaid) <= 0) { alert('Please enter the amount paid.'); return }
    if (!method) { alert('Please select a payment method.'); return }
    if (method === 'other' && !otherMethod.trim()) { alert('Please specify the payment mode.'); return }

    setSubmitting(true)
    try {
      await onSubmit({
        screenshot,
        amountPaid: Number(amountPaid),
        paymentMethod: method === 'other' ? otherMethod.trim() : method,
        utrRef: utrRef.trim() || undefined,
        notes: notes.trim() || undefined,
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h2 className="text-lg font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          {claimsToPay.length === 1 ? 'Pay Claim' : `Pay ${claimsToPay.length} Claims`} — {userName}
        </h2>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          {claimsToPay.length} claim{claimsToPay.length !== 1 ? 's' : ''} · Total ₹{formatRupees(totalAmount)}
        </p>
      </div>

      <Card padded={false} className="max-w-lg">
        {/* Claims breakdown */}
        <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border">
          <p className="text-[10px] font-semibold uppercase tracking-widest text-ink-light-muted dark:text-ink-dark-muted mb-2">
            Claims Being Paid
          </p>
          <div className="flex flex-col divide-y divide-surface-light-border dark:divide-surface-dark-border">
            {claimsToPay.map(c => (
              <div key={c.id} className="flex items-center justify-between py-1.5">
                <div className="min-w-0">
                  <span className="text-xs text-ink-light-primary dark:text-ink-dark-primary font-medium">{c.campaign}</span>
                  <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted ml-2 font-mono">{c.id}</span>
                </div>
                <span className="text-xs font-semibold text-neon-orange ml-4 flex-shrink-0">₹{formatRupees(c.amount)}</span>
              </div>
            ))}
          </div>
          <div className="flex items-center justify-between mt-2 pt-2 border-t border-surface-light-border dark:border-surface-dark-border">
            <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">Total Payable</span>
            <span className="text-base font-bold text-neon-orange">₹{formatRupees(totalAmount)}</span>
          </div>
        </div>

        {/* Form */}
        <div className="px-4 py-4 flex flex-col gap-4">
          {/* Screenshot upload */}
          <ScreenshotUpload
            label="Payment Screenshot"
            onFileChange={file => setScreenshot(file)}
          />

          {/* Amount paid */}
          <div>
            <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
              Amount Paid <span className="text-neon-red">*</span>
            </label>
            <RupeeInput
              value={amountPaid}
              onChange={setAmountPaid}
              className={inputClass}
            />
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-1">Expected: ₹{formatRupees(totalAmount)}</p>
          </div>

          {/* Payment method */}
          <div>
            <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
              Payment Method <span className="text-neon-red">*</span>
            </label>
            <select
              value={method}
              onChange={e => setMethod(e.target.value)}
              className={inputClass}
            >
              <option value="">Select payment method</option>
              {PAYMENT_METHODS.map(m => (
                <option key={m.value} value={m.value}>{m.label}</option>
              ))}
            </select>
          </div>

          {method === 'other' && (
            <div>
              <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
                Specify Payment Mode <span className="text-neon-red">*</span>
              </label>
              <input
                type="text"
                value={otherMethod}
                onChange={e => setOtherMethod(e.target.value)}
                placeholder="e.g. Cheque, Demand Draft…"
                className={inputClass}
              />
            </div>
          )}

          {/* UTR */}
          <div>
            <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
              Reference / UTR <span className="text-ink-light-muted dark:text-ink-dark-muted font-normal">(optional)</span>
            </label>
            <input
              type="text"
              value={utrRef}
              onChange={e => setUtrRef(e.target.value)}
              placeholder="Transaction ID or UTR number"
              className={inputClass}
            />
          </div>

          {/* Notes */}
          <div>
            <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
              Notes <span className="text-ink-light-muted dark:text-ink-dark-muted font-normal">(optional)</span>
            </label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value)}
              placeholder="Any remarks for this payment…"
              rows={2}
              className={[inputClass, 'resize-none'].join(' ')}
            />
          </div>

          {/* Actions */}
          <div className="flex gap-2 pt-1">
            <Button variant="primary" size="lg" onClick={handleSubmit} loading={submitting} className="flex-1">
              Confirm &amp; Mark as Paid
            </Button>
          </div>
        </div>
      </Card>
    </div>
  )
}
