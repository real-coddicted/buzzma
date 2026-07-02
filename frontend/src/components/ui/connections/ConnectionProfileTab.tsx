import { useState, useEffect } from 'react'
import { DetailsCard } from '../DetailsCard'
import { Loading } from '../Loading'
import { fetchUserById, fetchUserBanking } from '../../../api/userApi'
import type { UserSummaryDto } from '../../../api/userApi'
import type { UserBankingDto, UserDetails } from '../../../types/ProfileTypes'

const roleTypeMap: Record<NonNullable<UserSummaryDto['role']>, UserDetails['type']> = {
  ROLE_BRAND:    'brand',
  ROLE_AGENCY:   'agency',
  ROLE_MEDIATOR: 'mediator',
  ROLE_BUYER:    'buyer',
  ROLE_ADMIN:    'admin',
}

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

function BankDetailsCard({ banking, loading }: { banking: UserBankingDto | null; loading: boolean }) {
  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
      <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary mb-5">
        Bank Details
      </h3>
      {loading ? (
        <div className="flex justify-center py-6">
          <Loading size={24} />
        </div>
      ) : banking ? (
        <div className="space-y-4">
          <Field label="Bank Name"        value={banking.bankName ?? ''} />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <Field label="Account Number"   value={banking.bankAccountNumber ?? ''} />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <Field label="IFSC Code"        value={banking.bankIfscCode ?? ''} />
          <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          <Field label="Account Holder"   value={banking.bankAccountHolderName ?? ''} />
        </div>
      ) : (
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
          Bank details not available.
        </p>
      )}
    </div>
  )
}

interface ConnectionProfileTabProps {
  toUserId: string
  name: string
}

export function ConnectionProfileTab({ toUserId, name }: ConnectionProfileTabProps) {
  const [profile, setProfile]           = useState<UserSummaryDto | null>(null)
  const [banking, setBanking]           = useState<UserBankingDto | null>(null)
  const [profileLoading, setProfileLoading] = useState(true)
  const [bankingLoading, setBankingLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setProfileLoading(true)
    fetchUserById(toUserId)
      .then(d => { if (!cancelled) setProfile(d) })
      .catch(() => { if (!cancelled) setProfile(null) })
      .finally(() => { if (!cancelled) setProfileLoading(false) })
    return () => { cancelled = true }
  }, [toUserId])

  useEffect(() => {
    let cancelled = false
    setBankingLoading(true)
    fetchUserBanking(toUserId)
      .then(d => { if (!cancelled) setBanking(d) })
      .catch(() => { if (!cancelled) setBanking(null) })
      .finally(() => { if (!cancelled) setBankingLoading(false) })
    return () => { cancelled = true }
  }, [toUserId])

  const userDetails: UserDetails = {
    type:   profile?.role ? roleTypeMap[profile.role] : 'buyer',
    name:   profile?.name ?? name,
    mobile: profile?.mobile ?? '',
    email:  profile?.email ?? undefined,
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {profileLoading ? (
        <div className="flex justify-center py-10">
          <Loading size={24} />
        </div>
      ) : (
        <DetailsCard details={userDetails} />
      )}
      <BankDetailsCard banking={banking} loading={bankingLoading} />
    </div>
  )
}
