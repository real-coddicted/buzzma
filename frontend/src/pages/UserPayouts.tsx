import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { UserPayoutsList } from '../components/ui/user-payouts/UserPayoutsList'
import { UserPayoutsDetail } from '../components/ui/user-payouts/UserPayoutsDetail'
import { PaymentForm } from '../components/ui/user-payouts/PaymentForm'
import { fetchPayoutUsers, fetchPayoutClaims, submitPayment } from '../api/userPayoutsApi'
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
  const [detailLoading, setDetailLoading] = useState(false)
  const [toast, setToast]             = useState<{ message: string; type: 'success' | 'error' } | null>(null)

  const loadedUserIds = useRef(new Set<string>())

  useEffect(() => {
    fetchPayoutUsers()
      .then(setUsers)
      .finally(() => setLoading(false))
  }, [])

  // Lazy-load claims the first time we navigate to a specific user's detail or pay-form.
  useEffect(() => {
    if (!userId || loadedUserIds.current.has(userId)) return
    loadedUserIds.current.add(userId)
    setDetailLoading(true)
    fetchPayoutClaims(userId)
      .then(claims => setClaimsData(prev => ({ ...prev, [userId]: claims })))
      .finally(() => setDetailLoading(false))
  }, [userId])

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

    // Keep user-level summary in sync so the list view reflects the payment.
    const paidAmount = [...paidIds].reduce((s, id) => {
      const c = currentClaims.find(x => x.id === id)
      return s + (c?.amount ?? 0)
    }, 0)
    setUsers(prev => prev.map(u =>
      u.id === userId
        ? { ...u, claimCount: u.claimCount - paidIds.size, totalAmount: u.totalAmount - paidAmount }
        : u,
    ))

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
    users.filter(u => u.claimCount > 0).length / PAGE_SIZE
  )

  return (
    <>
      {view === 'list' && (
        <UserPayoutsList
          users={users}
          page={listPage}
          onPageChange={p => setListPage(Math.max(1, Math.min(p, totalPages)))}
          onOpenDetail={openDetail}
          onPayUser={user => {
            setSearchParams({ view: 'pay-form', id: user.id, mode: 'all' })
          }}
        />
      )}

      {view === 'detail' && currentUser && (
        detailLoading ? <Loading /> : (
          <UserPayoutsDetail
            user={currentUser}
            claims={currentClaims}
            selectedClaims={selectedClaims}
            onToggleClaim={toggleClaim}
            onToggleAll={toggleAllClaims}
            onPayAll={() => openPayForm('all')}
            onPaySelected={() => openPayForm('selected')}
          />
        )
      )}

      {view === 'pay-form' && currentUser && (
        detailLoading ? <Loading /> : (
          <PaymentForm
            userName={currentUser.name}
            claimsToPay={claimsToPay}
            onSubmit={handleSubmit}
          />
        )
      )}

      {toast && (
        <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />
      )}
    </>
  )
}
