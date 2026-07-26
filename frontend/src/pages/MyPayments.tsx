import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Tabs } from '../components/ui/Tabs'
import { PaginationToolbar } from '../components/ui/PaginationToolbar'
import { IconCalendar } from '../components/ui/icons'
import { StatBanner } from '../components/ui/my-payments/StatBanner'
import { BatchRow } from '../components/ui/my-payments/BatchRow'
import { ProofModal } from '../components/ui/my-payments/ProofModal'
import { AgencyRow } from '../components/ui/my-payments/AgencyRow'
import { BatchDetail } from '../components/ui/my-payments/BatchDetail'
import { AgencyDetail } from '../components/ui/my-payments/AgencyDetail'
import {
  fetchPaymentBatches,
  fetchPaymentClaims,
  fetchPendingAgencies,
  fetchPendingClaims,
} from '../api/myPaymentsApi'
import type { PaymentBatch, PaymentClaim, PendingAgency, PendingClaim } from '../types/MyPaymentsTypes'

type Tab = 'received' | 'awaited'

const PAGE_SIZE = 5

export function MyPayments() {
  const [searchParams, setSearchParams] = useSearchParams()

  const tab = (searchParams.get('tab') as Tab) ?? 'received'
  const view = searchParams.get('view')
  const detailId = searchParams.get('id')

  const [batches, setBatches] = useState<PaymentBatch[]>([])
  const [agencies, setAgencies] = useState<PendingAgency[]>([])
  const [listLoading, setListLoading] = useState(true)

  const [batchClaims, setBatchClaims] = useState<PaymentClaim[]>([])
  const [pendingClaims, setPendingClaims] = useState<PendingClaim[]>([])
  const [detailLoading, setDetailLoading] = useState(false)

  const [receivedPage, setReceivedPage] = useState(1)
  const [awaitedPage, setAwaitedPage] = useState(1)
  const [proofBatch, setProofBatch] = useState<PaymentBatch | null>(null)

  useEffect(() => {
    Promise.all([fetchPaymentBatches(), fetchPendingAgencies()])
      .then(([b, a]) => { setBatches(b); setAgencies(a) })
      .finally(() => setListLoading(false))
  }, [])

  useEffect(() => {
    if (view === 'batch' && detailId) {
      setDetailLoading(true)
      fetchPaymentClaims(detailId)
        .then(setBatchClaims)
        .finally(() => setDetailLoading(false))
    } else if (view === 'agency' && detailId) {
      setDetailLoading(true)
      fetchPendingClaims(detailId)
        .then(setPendingClaims)
        .finally(() => setDetailLoading(false))
    }
  }, [view, detailId])

  function setTab(t: Tab) {
    setSearchParams(prev => { prev.set('tab', t); prev.delete('view'); prev.delete('id'); return prev })
  }

  function openBatch(batch: PaymentBatch) {
    setSearchParams(prev => { prev.set('view', 'batch'); prev.set('id', batch.id); return prev })
  }

  function openAgency(agency: PendingAgency) {
    setSearchParams(prev => { prev.set('view', 'agency'); prev.set('id', agency.id); return prev })
  }

  function goBack() {
    setSearchParams(prev => { prev.delete('view'); prev.delete('id'); return prev })
  }

  if (listLoading) return <Loading />

  if (view === 'batch') {
    const batch = batches.find(b => b.id === detailId)
    return <BatchDetail batch={batch} claims={batchClaims} loading={detailLoading} onBack={goBack} />
  }

  if (view === 'agency') {
    const agency = agencies.find(a => a.id === detailId)
    return <AgencyDetail agency={agency} claims={pendingClaims} loading={detailLoading} onBack={goBack} />
  }

  const totalReceived = batches.reduce((s, b) => s + b.totalAmount, 0)
  const totalAwaited = agencies.reduce((s, a) => s + a.totalPendingAmount, 0)
  const totalPendingClaims = agencies.reduce((s, a) => s + a.pendingClaimCount, 0)

  const receivedTotalPages = Math.ceil(batches.length / PAGE_SIZE)
  const awaitedTotalPages = Math.ceil(agencies.length / PAGE_SIZE)
  const pagedBatches = batches.slice((receivedPage - 1) * PAGE_SIZE, receivedPage * PAGE_SIZE)
  const pagedAgencies = agencies.slice((awaitedPage - 1) * PAGE_SIZE, awaitedPage * PAGE_SIZE)

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-ink-light-primary dark:text-ink-dark-primary">My Payments</h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          Track all payments received and pending against your claims
        </p>
      </div>

      <Tabs<Tab>
        options={[
          { value: 'received', label: 'Payments Received' },
          { value: 'awaited', label: 'Payments Awaited' },
        ]}
        value={tab}
        onChange={setTab}
      />

      {tab === 'received' && (
        <div className="flex flex-col gap-4">
          <StatBanner
            label="Total Received"
            amount={totalReceived}
            subtitle={`across ${batches.length} payment dates`}
            rightCount={batches.reduce((s, b) => s + b.claimCount, 0)}
            rightLabel="Total Claims"
            accent="green"
          />
          <Card padded={false}>
            <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border flex items-center gap-2">
              <IconCalendar className="text-ink-light-muted dark:text-ink-dark-muted" />
              <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">Payment History</span>
            </div>
            <ul>
              {pagedBatches.map((batch, i) => (
                <BatchRow
                  key={batch.id}
                  batch={batch}
                  isLast={i === pagedBatches.length - 1}
                  onClick={() => openBatch(batch)}
                  onProofClick={setProofBatch}
                />
              ))}
            </ul>
            <PaginationToolbar
              currentPage={receivedPage}
              totalPages={receivedTotalPages}
              onPageChange={setReceivedPage}
            />
          </Card>
        </div>
      )}

      {proofBatch && <ProofModal batch={proofBatch} onClose={() => setProofBatch(null)} />}

      {tab === 'awaited' && (
        <div className="flex flex-col gap-4">
          <StatBanner
            label="Total Awaited"
            amount={totalAwaited}
            subtitle={`across ${agencies.length} agencies`}
            rightCount={totalPendingClaims}
            rightLabel="Pending Claims"
            accent="orange"
          />
          <Card padded={false}>
            <div className="px-4 py-3 border-b border-surface-light-border dark:border-surface-dark-border">
              <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary">Pending by Agency</span>
              <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-0.5">Click to view pending claims</p>
            </div>
            <ul>
              {pagedAgencies.map((agency, i) => (
                <AgencyRow
                  key={agency.id}
                  agency={agency}
                  isLast={i === pagedAgencies.length - 1}
                  onClick={() => openAgency(agency)}
                />
              ))}
            </ul>
            <PaginationToolbar
              currentPage={awaitedPage}
              totalPages={awaitedTotalPages}
              onPageChange={setAwaitedPage}
            />
          </Card>
        </div>
      )}
    </div>
  )
}
