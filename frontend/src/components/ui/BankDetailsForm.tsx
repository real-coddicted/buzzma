import { useState, useEffect } from 'react'
import { Button } from './Button'
import { Toast } from './Toast'
import { fetchUserBanking, upsertUserBanking } from '../../api/userApi'
import type { UserBankingDetailDto } from '../../api/userApi'

const inputBase =
  'w-full rounded-lg border bg-surface-light-base dark:bg-surface-dark-hover ' +
  'border-surface-light-border dark:border-surface-dark-border ' +
  'text-ink-light-primary dark:text-ink-dark-primary ' +
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted ' +
  'px-3 py-2.5 text-sm outline-none transition-colors ' +
  'focus:border-neon-blue focus:ring-1 focus:ring-neon-blue/30'

type FormErrors = {
  bankName?: string
  bankAccountNumber?: string
  bankIfscCode?: string
  bankAccountHolderName?: string
}

interface BankDetailsFormProps {
  userId: string
}

export function BankDetailsForm({ userId }: BankDetailsFormProps) {
  const [bankName, setBankName]                           = useState('')
  const [bankAccountNumber, setBankAccountNumber]         = useState('')
  const [bankIfscCode, setBankIfscCode]                   = useState('')
  const [bankAccountHolderName, setBankAccountHolderName] = useState('')
  const [upiId, setUpiId]                                 = useState('')
  const [upiMobileNumber, setUpiMobileNumber]             = useState('')
  const [errors, setErrors]   = useState<FormErrors>({})
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [apiError, setApiError] = useState('')

  useEffect(() => {
    fetchUserBanking(userId)
      .then((data: UserBankingDetailDto) => {
        setBankName(data.bankName ?? '')
        setBankAccountNumber(data.bankAccountNumber ?? '')
        setBankIfscCode(data.bankIfscCode ?? '')
        setBankAccountHolderName(data.bankAccountHolderName ?? '')
        setUpiId(data.upiId ?? '')
        setUpiMobileNumber(data.upiMobileNumber ?? '')
      })
      .catch(() => {
        // No existing record — form stays empty
      })
  }, [userId])

  function validate(): boolean {
    const next: FormErrors = {}
    if (!bankName.trim())              next.bankName = 'Bank name is required'
    if (!bankAccountNumber.trim())     next.bankAccountNumber = 'Account number is required'
    if (!bankIfscCode.trim())          next.bankIfscCode = 'IFSC code is required'
    if (!bankAccountHolderName.trim()) next.bankAccountHolderName = 'Account holder name is required'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    setApiError('')
    setSuccess(false)
    try {
      await upsertUserBanking(userId, {
        bankName,
        bankAccountNumber,
        bankIfscCode,
        bankAccountHolderName,
        upiId: upiId || undefined,
        upiMobileNumber: upiMobileNumber || undefined,
      })
      setSuccess(true)
    } catch (err) {
      setApiError(err instanceof Error ? err.message : 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
      <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-5">
        Bank Details
      </h3>
      <form onSubmit={handleSubmit} noValidate className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
            Bank Name
          </label>
          <input
            type="text"
            placeholder="e.g. HDFC Bank"
            value={bankName}
            onChange={e => { setBankName(e.target.value); setErrors(p => ({ ...p, bankName: undefined })) }}
            className={inputBase}
          />
          {errors.bankName && <p className="mt-1 text-xs text-neon-red">{errors.bankName}</p>}
        </div>
        <div>
          <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
            Account Number
          </label>
          <input
            type="text"
            placeholder="Your account number"
            value={bankAccountNumber}
            onChange={e => { setBankAccountNumber(e.target.value); setErrors(p => ({ ...p, bankAccountNumber: undefined })) }}
            className={inputBase}
          />
          {errors.bankAccountNumber && <p className="mt-1 text-xs text-neon-red">{errors.bankAccountNumber}</p>}
        </div>
        <div>
          <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
            IFSC Code
          </label>
          <input
            type="text"
            placeholder="e.g. HDFC0001234"
            value={bankIfscCode}
            onChange={e => { setBankIfscCode(e.target.value.toUpperCase()); setErrors(p => ({ ...p, bankIfscCode: undefined })) }}
            className={inputBase}
          />
          {errors.bankIfscCode && <p className="mt-1 text-xs text-neon-red">{errors.bankIfscCode}</p>}
        </div>
        <div>
          <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
            Account Holder Name
          </label>
          <input
            type="text"
            placeholder="Name as on bank account"
            value={bankAccountHolderName}
            onChange={e => { setBankAccountHolderName(e.target.value); setErrors(p => ({ ...p, bankAccountHolderName: undefined })) }}
            className={inputBase}
          />
          {errors.bankAccountHolderName && <p className="mt-1 text-xs text-neon-red">{errors.bankAccountHolderName}</p>}
        </div>

        <div className="border-t border-surface-light-border dark:border-surface-dark-border pt-4">
          <p className="text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-3">
            UPI (Optional)
          </p>
          <div className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
                UPI ID
              </label>
              <input
                type="text"
                placeholder="e.g. name@upi"
                value={upiId}
                onChange={e => setUpiId(e.target.value)}
                className={inputBase}
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide mb-1.5">
                UPI Mobile Number
              </label>
              <input
                type="tel"
                placeholder="Mobile number linked to UPI"
                value={upiMobileNumber}
                onChange={e => setUpiMobileNumber(e.target.value)}
                className={inputBase}
              />
            </div>
          </div>
        </div>

        {apiError && (
          <Toast
            type="error"
            message={apiError}
            onDismiss={() => setApiError('')}
          />
        )}
        {success && (
          <Toast
            message="Bank details saved successfully."
            onDismiss={() => setSuccess(false)}
          />
        )}
        <div className="flex justify-end">
          <Button type="submit" variant="primary" loading={loading}>
            Save Bank Details
          </Button>
        </div>
      </form>
    </div>
  )
}
