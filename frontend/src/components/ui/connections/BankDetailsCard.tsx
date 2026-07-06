import { Loading } from '../Loading'
import { LabeledField } from '../LabeledField'
import type { UserBankingDetailDto } from '../../../api/userApi'

interface BankDetailsCardProps {
  banking: UserBankingDetailDto | null
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
        <div className="space-y-3">
          <LabeledField label="Bank Name"      value={banking?.bankName ?? ''} copyable />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <LabeledField label="Account Number" value={banking?.bankAccountNumber ?? ''} masked copyable />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <LabeledField label="IFSC Code"      value={banking?.bankIfscCode ?? ''} masked copyable />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <LabeledField label="Account Holder" value={banking?.bankAccountHolderName ?? ''} />
          {(banking?.upiId || banking?.upiMobileNumber) && (
            <>
              <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
              {banking?.upiId && (
                <LabeledField label="UPI ID" value={banking.upiId} masked copyable />
              )}
              {banking?.upiMobileNumber && (
                <>
                  <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
                  <LabeledField label="UPI Mobile" value={banking.upiMobileNumber} copyable />
                </>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}
