import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { Tabs } from '../components/ui/Tabs'
import { UserPayoutsList } from '../components/ui/user-payouts/UserPayoutsList'
import { UserPayoutsDetail } from '../components/ui/user-payouts/UserPayoutsDetail'
import { PaymentForm } from '../components/ui/user-payouts/PaymentForm'
import { PaidPayeesList } from '../components/ui/user-payouts/PaidPayeesList'
import { PayeePaymentsList } from '../components/ui/user-payouts/PayeePaymentsList'
import { PaymentClaimsDetail } from '../components/ui/user-payouts/PaymentClaimsDetail'
import { PayoutProofModal } from '../components/ui/user-payouts/PayoutProofModal'
import {
  fetchPayoutUsers,
  fetchPayoutClaims,
  submitPayment,
  fetchPaidPayees,
  fetchPaymentsForPayee,
  fetchClaimsForPayment,
} from '../api/userPayoutsApi'
import type { PayoutUser, PayoutClaim, PaymentSubmission, PaidPayee, MadePayment } from '../types/UserPayoutsTypes'

type Tab     = 'pending' | 'paid'
type View    = 'list' | 'detail' | 'pay-form' | 'payments' | 'claims'
type PayMode = 'all' | 'selected'

const PAGE_SIZE = 5

export function UserPayouts() {
  const [searchParams, setSearchParams] = useSearchParams()

  const tab     = (searchParams.get('tab') as Tab)   ?? 'pending'
  const view    = (searchParams.get('view') as View) ?? 'list'
  const userId  = searchParams.get('id')              ?? ''
  const payMode = (searchParams.get('mode') as PayMode) ?? 'all'

  // ---- Pending payouts ----
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

  // Lazy-load the claims the first time we navigate to a specific user's detail or pay-form.
  useEffect(() => {
    if (tab !== 'pending' || !userId || loadedUserIds.current.has(userId)) return
    loadedUserIds.current.add(userId)
    setDetailLoading(true)
    fetchPayoutClaims(userId)
      .then(claims => setClaimsData(prev => ({ ...prev, [userId]: claims })))
      .finally(() => setDetailLoading(false))
  }, [tab, userId])

  // ---- Paid payout history (Issue#512) ----
  const [paidPayees, setPaidPayees]         = useState<PaidPayee[]>([])
  const [paidTotal, setPaidTotal]           = useState(0)
  const [paidTotalPages, setPaidTotalPages] = useState(1)
  const [paidPage, setPaidPage]             = useState(1)
  const [paidLoading, setPaidLoading]       = useState(false)

  const [selectedPayee, setSelectedPayee]           = useState<PaidPayee | null>(null)
  const [payments, setPayments]                     = useState<MadePayment[]>([])
  const [paymentsTotalPages, setPaymentsTotalPages] = useState(1)
  const [paymentsPage, setPaymentsPage]             = useState(1)
  const [paymentsLoading, setPaymentsLoading]       = useState(false)

  const [selectedPayment, setSelectedPayment]   = useState<MadePayment | null>(null)
  const [paymentClaims, setPaymentClaims]       = useState<PayoutClaim[]>([])
  const [claimsTotalPages, setClaimsTotalPages] = useState(1)
  const [claimsPage, setClaimsPage]             = useState(1)
  const [claimsLoading, setClaimsLoading]       = useState(false)

  const [proofPayment, setProofPayment] = useState<MadePayment | null>(null)

  // Lazy-load paid payees the first time the Paid tab is opened, and on every page change.
  useEffect(() => {
    if (tab !== 'paid') return
    setPaidLoading(true)
    fetchPaidPayees(paidPage, PAGE_SIZE)
      .then(({ items, total, totalPages }) => {
        setPaidPayees(items)
        setPaidTotal(total)
        setPaidTotalPages(totalPages)
      })
      .finally(() => setPaidLoading(false))
  }, [tab, paidPage])

  useEffect(() => {
    if (tab !== 'paid' || view !== 'payments' || !selectedPayee) return
    setPaymentsLoading(true)
    fetchPaymentsForPayee(selectedPayee.id, paymentsPage, PAGE_SIZE)
      .then(({ items, totalPages }) => {
        setPayments(items)
        setPaymentsTotalPages(totalPages)
      })
      .finally(() => setPaymentsLoading(false))
  }, [tab, view, selectedPayee, paymentsPage])

  useEffect(() => {
    if (tab !== 'paid' || view !== 'claims' || !selectedPayment) return
    setClaimsLoading(true)
    fetchClaimsForPayment(selectedPayment.id, claimsPage, PAGE_SIZE)
      .then(({ items, totalPages }) => {
        setPaymentClaims(items)
        setClaimsTotalPages(totalPages)
      })
      .finally(() => setClaimsLoading(false))
  }, [tab, view, selectedPayment, claimsPage])

  function setTab(t: Tab) {
    setSearchParams(prev => {
      const p = new URLSearchParams(prev)
      p.set('tab', t)
      p.delete('view')
      p.delete('id')
      p.delete('mode')
      return p
    })
    setSelectedPayee(null)
    setSelectedPayment(null)
    setPaidPage(1)
  }

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

  function openPaidPayee(payee: PaidPayee) {
    setSelectedPayee(payee)
    setPaymentsPage(1)
    setSearchParams(prev => {
      const p = new URLSearchParams(prev)
      p.set('view', 'payments')
      p.set('id', payee.id)
      return p
    })
  }

  function openPayment(payment: MadePayment) {
    setSelectedPayment(payment)
    setClaimsPage(1)
    setSearchParams(prev => {
      const p = new URLSearchParams(prev)
      p.set('view', 'claims')
      p.set('id', payment.id)
      return p
    })
  }

  function backToPaidList() {
    setSelectedPayee(null)
    setSearchParams(prev => {
      const p = new URLSearchParams(prev)
      p.delete('view')
      p.delete('id')
      return p
    })
  }

  function backToPayeePayments() {
    setSelectedPayment(null)
    setSearchParams(prev => {
      const p = new URLSearchParams(prev)
      p.set('view', 'payments')
      if (selectedPayee) p.set('id', selectedPayee.id)
      return p
    })
  }

  const showFullPageLoading = tab === 'pending' ? loading : (paidLoading && paidPayees.length === 0)
  if (showFullPageLoading) return <Loading />

  if (tab === 'paid' && view === 'claims') {
    return (
      <PaymentClaimsDetail
        payment={selectedPayment ?? undefined}
        claims={paymentClaims}
        page={claimsPage}
        totalPages={claimsTotalPages}
        loading={claimsLoading}
        onPageChange={setClaimsPage}
        onBack={backToPayeePayments}
      />
    )
  }

  if (tab === 'paid' && view === 'payments') {
    return (
      <>
        <PayeePaymentsList
          payee={selectedPayee ?? undefined}
          payments={payments}
          page={paymentsPage}
          totalPages={paymentsTotalPages}
          loading={paymentsLoading}
          onPageChange={setPaymentsPage}
          onBack={backToPaidList}
          onOpenPayment={openPayment}
          onViewProof={setProofPayment}
        />
        {proofPayment && <PayoutProofModal payment={proofPayment} onClose={() => setProofPayment(null)} />}
      </>
    )
  }

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
        <div className="flex flex-col gap-6">
          <div>
            <h1 className="text-xl font-semibold text-ink-light-primary dark:text-ink-dark-primary">User Payouts</h1>
            <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
              Manage payouts to your downward network
            </p>
          </div>

          <Tabs<Tab>
            options={[
              { value: 'pending', label: 'Pending Payouts' },
              { value: 'paid',    label: 'Payment History' },
            ]}
            value={tab}
            onChange={setTab}
          />

          {tab === 'pending' && (
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

          {tab === 'paid' && (
            <PaidPayeesList
              payees={paidPayees}
              total={paidTotal}
              page={paidPage}
              totalPages={paidTotalPages}
              loading={paidLoading}
              onPageChange={setPaidPage}
              onOpenPayee={openPaidPayee}
            />
          )}
        </div>
      )}

      {tab === 'pending' && view === 'detail' && currentUser && (
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

      {tab === 'pending' && view === 'pay-form' && currentUser && (
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
