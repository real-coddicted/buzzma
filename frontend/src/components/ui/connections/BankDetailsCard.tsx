import { Loading } from '../Loading'
import type { UserBankingDto } from '../../../types/ProfileTypes'

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide">
        {label}
      </span>
      <span className="text-sm text-ink-light-primary dark:text-ink-dark-primary">
        {value || '—'}
      </span>
    </div>
  )
}

interface BankDetailsCardProps {
  banking: UserBankingDto | null
  loading: boolean
}

export function BankDetailsCard({ banking, loading }: BankDetailsCardProps) {
  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
      <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-5">
        Bank Details
      </h3>
      {loading ? (
        <div className="flex justify-center py-6">
          <Loading size={24} />
        </div>
      ) : (
        <div className="space-y-4">
          <Field label="Bank Name"      value={banking?.bankName ?? ''} />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <Field label="Account Number" value={banking?.bankAccountNumber ?? ''} />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <Field label="IFSC Code"      value={banking?.bankIfscCode ?? ''} />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <Field label="Account Holder" value={banking?.bankAccountHolderName ?? ''} />
        </div>
      )}
    </div>
  )
}
