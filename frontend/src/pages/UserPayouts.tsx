import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { UserPayoutsList } from '../components/ui/user-payouts/UserPayoutsList'
import { UserPayoutsDetail } from '../components/ui/user-payouts/UserPayoutsDetail'
import { PaymentForm } from '../components/ui/user-payouts/PaymentForm'
import { fetchPayoutUsers, fetchAllPayoutClaims, submitPayment } from '../api/userPayoutsApi'
import type { PayoutUser, PayoutClaim, PaymentSubmission } from '../types/UserPayoutsTypes'

type View   = 'list' | 'detail' | 'pay-form'
type PayMode = 'all' | 'selected'

const PAGE_SIZE = 5

export function UserPayouts() {
  const [searchParams, setSearchParams] = useSearchParams()

  const view    = (searchParams.get('view') as View)    ?? 'list'
  const userId  = searchParams.get('id')                ?? ''
  const payMode = (searchParams.get('mode') as PayMode) ?? 'all'

  const [users, setUsers]             = useState<PayoutUser[]>([])
  const [claimsData, setClaimsData]   = useState<Record<string, PayoutClaim[]>>({})
  const [selectedClaims, setSelected] = useState<Set<string>>(new Set())
  const [listPage, setListPage]       = useState(1)
  const [loading, setLoading]         = useState(true)
  const [toast, setToast]             = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  useEffect(() => {
    Promise.all([fetchPayoutUsers(), fetchAllPayoutClaims()])
      .then(([u, c]) => { setUsers(u); setClaimsData(c) })
      .finally(() => setLoading(false))
  }, [])

  function openDetail(user: PayoutUser) {
    setSelected(new Set())
    setSearchParams({ view: 'detail', id: user.id })
  }

  function openPayForm(mode: PayMode) {
    setSearchParams(prev => {
      const p = new URLSearchParams(prev)
      p.set('view', 'pay-form')
      p.set('mode', mode)
      return p
    })
  }

  function toggleClaim(id: string) {
    setSelected(prev => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id); else next.add(id)
      return next
    })
  }

  function toggleAllClaims(checked: boolean) {
    const claims = claimsData[userId] ?? []
    setSelected(checked ? new Set(claims.map(c => c.id)) : new Set())
  }

  async function handleSubmit(submission: PaymentSubmission) {
    const currentClaims = claimsData[userId] ?? []
    const paidIds = payMode === 'all'
      ? new Set(currentClaims.map(c => c.id))
      : new Set(selectedClaims)

    await submitPayment(userId, [...paidIds], submission)

    const remaining = currentClaims.filter(c => !paidIds.has(c.id))
    setClaimsData(prev => ({ ...prev, [userId]: remaining }))
    setSelected(new Set())

    const count = paidIds.size
    setToast({ message: `${count} claim${count > 1 ? 's' : ''} marked as paid!`, type: 'success' })

    if (remaining.length === 0) {
      setSearchParams({})
    } else {
      setSearchParams({ view: 'detail', id: userId })
    }
  }

  if (loading) return <Loading />

  const currentUser    = users.find(u => u.id === userId)
  const currentClaims  = claimsData[userId] ?? []
  const claimsToPay    = payMode === 'all'
    ? currentClaims
    : currentClaims.filter(c => selectedClaims.has(c.id))

  const totalPages = Math.ceil(
    users.filter(u => (claimsData[u.id] ?? []).length > 0).length / PAGE_SIZE
  )

  return (
    <>
      {view === 'list' && (
        <UserPayoutsList
          users={users}
          claimsData={claimsData}
          page={listPage}
          onPageChange={p => setListPage(Math.max(1, Math.min(p, totalPages)))}
          onOpenDetail={openDetail}
          onPayUser={user => {
            // set userId in params then open pay-form (all mode)
            setSearchParams({ view: 'pay-form', id: user.id, mode: 'all' })
          }}
        />
      )}

      {view === 'detail' && currentUser && (
        <UserPayoutsDetail
          user={currentUser}
          claims={currentClaims}
          selectedClaims={selectedClaims}
          onToggleClaim={toggleClaim}
          onToggleAll={toggleAllClaims}
          onPayAll={() => openPayForm('all')}
          onPaySelected={() => openPayForm('selected')}
        />
      )}

      {view === 'pay-form' && currentUser && (
        <PaymentForm
          userName={currentUser.name}
          claimsToPay={claimsToPay}
          onSubmit={handleSubmit}
        />
      )}

      {toast && (
        <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />
      )}
    </>
  )
}
