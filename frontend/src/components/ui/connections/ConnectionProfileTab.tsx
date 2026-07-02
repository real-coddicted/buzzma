import { useState, useEffect } from 'react'
import { DetailsCard } from '../DetailsCard'
import { Loading } from '../Loading'
import { BankDetailsCard } from './BankDetailsCard'
import { fetchUserById, fetchUserBanking } from '../../../api/userApi'
import type { UserSummaryDto } from '../../../api/userApi'
import type { UserBankingDto, UserDetails } from '../../../types/ProfileTypes'
import { roleToType } from '../../../utils/userRole'

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
