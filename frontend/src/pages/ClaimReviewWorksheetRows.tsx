import { useEffect, useState } from 'react'
import { useBreadcrumb } from '../contexts/BreadcrumbContext'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { PaginationToolbar } from '../components/ui/PaginationToolbar'
import {
  listClaimReviewWorksheetRows,
  type ClaimReviewWorksheetResponseDto,
  type ClaimReviewWorksheetRowResponseDto,
} from '../api/claimApi'
import { STATUS_CONFIG } from './ClaimReviewWorksheetImport'

interface ClaimReviewWorksheetRowsProps {
  worksheet: ClaimReviewWorksheetResponseDto
  onBack: () => void
}

const PAGE_SIZE = 20

const COLUMNS = [
  'Campaign', 'Platform', 'Order ID', 'Order Date', 'Order Amount',
  'Claim Code', 'Amount Approved', 'Brand Review', 'Processing Status', 'Error Remarks',
]

export function ClaimReviewWorksheetRows({ worksheet, onBack }: ClaimReviewWorksheetRowsProps) {
  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail(worksheet.originalFilename ?? 'Worksheet Rows', onBack)
    return clearDetail
  }, [worksheet.originalFilename, onBack, setDetail, clearDetail])

  const [rows, setRows] = useState<ClaimReviewWorksheetRowResponseDto[]>([])
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!worksheet.id) return
    let cancelled = false
    setLoading(true)
    listClaimReviewWorksheetRows(worksheet.id, currentPage, PAGE_SIZE)
      .then(data => {
        if (cancelled) return
        setRows(data.items)
        setTotalPages(data.totalPages)
      })
      .catch(err => { if (!cancelled) setError((err as Error).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [worksheet.id, currentPage])

  return (
    <div className="max-w-6xl mx-auto space-y-5">
      <Card padded={false}>
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
                {COLUMNS.map(col => (
                  <th key={col} className="px-5 py-3 text-left font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted whitespace-nowrap">
                    {col}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
              {loading ? (
                <tr>
                  <td colSpan={COLUMNS.length} className="px-5 py-10 text-center">
                    <div className="flex justify-center text-ink-light-muted dark:text-ink-dark-muted">
                      <Loading size={32} />
                    </div>
                  </td>
                </tr>
              ) : rows.length === 0 ? (
                <tr>
                  <td colSpan={COLUMNS.length} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                    No rows found for this worksheet.
                  </td>
                </tr>
              ) : (
                rows.map(r => (
                  <tr key={r.id} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.campaign ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.platform ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.orderId ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.orderDate ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.orderAmount ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.claimCode ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.amountApproved ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.brandReview ?? '—'}</td>
                    <td className="px-5 py-4">
                      {r.processingStatus
                        ? <span className={STATUS_CONFIG[r.processingStatus].colorClass}>{STATUS_CONFIG[r.processingStatus].label}</span>
                        : '—'}
                    </td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{r.errorRemarks ?? '—'}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        <PaginationToolbar currentPage={currentPage} totalPages={totalPages} onPageChange={setCurrentPage} disabled={loading} />
      </Card>
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}
