import { useState, useEffect } from 'react'
import { DetailsCard } from '../DetailsCard'
import { Loading } from '../Loading'
import { BankDetailsCard } from './BankDetailsCard'
import { fetchUserById, fetchUserBanking } from '../../../api/userApi'
import type { UserSummaryDto } from '../../../api/userApi'
import type { UserBankingDetailDto } from '../../../api/userApi'
import type { UserDetails } from '../../../types/ProfileTypes'
import { roleToType } from '../../../utils/userRole'

interface ConnectionProfileTabProps {
  toUserId: string
  name: string
  onError: (message: string) => void
}

export function ConnectionProfileTab({ toUserId, name, onError }: ConnectionProfileTabProps) {
  const [profile, setProfile]           = useState<UserSummaryDto | null>(null)
  const [banking, setBanking]           = useState<UserBankingDetailDto | null>(null)
  const [profileLoading, setProfileLoading] = useState(true)
  const [bankingLoading, setBankingLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setProfileLoading(true)
    fetchUserById(toUserId)
      .then(d => { if (!cancelled) setProfile(d) })
      .catch((err: unknown) => {
        if (!cancelled) {
          setProfile(null)
          onError(err instanceof Error ? err.message : 'Failed to load profile.')
        }
      })
      .finally(() => { if (!cancelled) setProfileLoading(false) })
    return () => { cancelled = true }
  }, [toUserId, onError])

  useEffect(() => {
    let cancelled = false
    setBankingLoading(true)
    fetchUserBanking(toUserId)
      .then(d => { if (!cancelled) setBanking(d) })
      .catch((err: unknown) => {
        if (!cancelled) {
          setBanking(null)
          onError(err instanceof Error ? err.message : 'Failed to load bank details.')
        }
      })
      .finally(() => { if (!cancelled) setBankingLoading(false) })
    return () => { cancelled = true }
  }, [toUserId, onError])

  const userDetails: UserDetails = {
    type:   roleToType(profile?.role),
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
