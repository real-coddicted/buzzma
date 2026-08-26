import { useEffect, useRef, useState } from 'react'
import { useBreadcrumb } from '../contexts/BreadcrumbContext'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { IconUpload, IconDownload, IconEye } from '../components/ui/icons'
import {
  listClaimReviewWorksheets,
  uploadClaimReviewWorksheet,
  downloadClaimReviewWorksheet,
  type ClaimReviewWorksheetResponseDto,
} from '../api/claimApi'
import { formatDateTime } from '../utils/time'

interface ClaimReviewWorksheetImportProps {
  onBack: () => void
  onOpenRows: (worksheet: ClaimReviewWorksheetResponseDto) => void
}

export type WorksheetStatus = NonNullable<ClaimReviewWorksheetResponseDto['status']>

export const STATUS_CONFIG: Record<WorksheetStatus, { label: string; colorClass: string }> = {
  PENDING:     { label: 'Pending',     colorClass: 'text-neon-yellow' },
  IN_PROGRESS: { label: 'In Progress', colorClass: 'text-neon-blue'   },
  SUCCESS:     { label: 'Success',     colorClass: 'text-neon-green'  },
  ERROR:       { label: 'Error',       colorClass: 'text-neon-red'    },
}

const COLUMNS = ['Original File Name', 'Total Rows', 'Rows Processed', 'Rows Pending', 'Status', 'Uploaded At', 'Actions']

export function ClaimReviewWorksheetImport({ onBack, onOpenRows }: ClaimReviewWorksheetImportProps) {
  const { setDetail, clearDetail } = useBreadcrumb()
  useEffect(() => {
    setDetail('Import Worksheets', onBack)
    return clearDetail
  }, [onBack, setDetail, clearDetail])

  const inputRef = useRef<HTMLInputElement>(null)
  const [file, setFile] = useState<File | null>(null)
  const [uploading, setUploading] = useState(false)
  const [worksheets, setWorksheets] = useState<ClaimReviewWorksheetResponseDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  function loadWorksheets() {
    setLoading(true)
    listClaimReviewWorksheets()
      .then(setWorksheets)
      .catch(err => setError((err as Error).message))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadWorksheets() }, [])

  function handleFile(f: File | undefined) {
    if (!f) return
    if (!f.name.toLowerCase().endsWith('.xlsx')) {
      setError('Please select an .xlsx file.')
      return
    }
    setFile(f)
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault()
    handleFile(e.dataTransfer.files[0])
  }

  async function handleUpload() {
    if (!file) return
    setUploading(true)
    try {
      await uploadClaimReviewWorksheet(file)
      setFile(null)
      if (inputRef.current) inputRef.current.value = ''
      loadWorksheets()
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setUploading(false)
    }
  }

  async function handleDownload(w: ClaimReviewWorksheetResponseDto) {
    if (!w.id) return
    try {
      await downloadClaimReviewWorksheet(w.id, w.originalFilename ?? 'worksheet.xlsx')
    } catch (err) {
      setError((err as Error).message)
    }
  }

  return (
    <div className="max-w-5xl mx-auto space-y-5">
      <Card>
        <div className="space-y-3">
          <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary">
            Worksheet file
          </label>
          <input
            type="text"
            readOnly
            value={file?.name ?? ''}
            placeholder="No file selected"
            className="w-full px-3 py-2 rounded-lg text-xs border border-surface-light-border dark:border-surface-dark-border bg-transparent text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
          />
          <div
            onClick={() => inputRef.current?.click()}
            onDrop={handleDrop}
            onDragOver={e => e.preventDefault()}
            className="w-full rounded-xl border-2 border-dashed border-surface-light-border dark:border-surface-dark-border hover:border-neon-blue/40 transition-colors cursor-pointer flex flex-col items-center gap-1.5 px-4 py-8 text-center"
          >
            <IconUpload size={22} />
            <span className="text-xs text-ink-light-muted dark:text-ink-dark-muted">
              Drop .xlsx file here or click to upload
            </span>
          </div>
          <input
            ref={inputRef}
            type="file"
            accept=".xlsx"
            className="hidden"
            onChange={e => handleFile(e.target.files?.[0])}
          />
          <button
            onClick={handleUpload}
            disabled={!file || uploading}
            className="px-4 py-2 rounded-lg text-xs font-semibold bg-neon-blue/10 border border-neon-blue/30 text-neon-blue hover:brightness-110 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {uploading ? 'Uploading…' : 'Upload'}
          </button>
        </div>
      </Card>

      <Card padded={false}>
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
                {COLUMNS.map(col => (
                  <th key={col} className="px-5 py-3 text-left font-semibold uppercase tracking-wider text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
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
              ) : worksheets.length === 0 ? (
                <tr>
                  <td colSpan={COLUMNS.length} className="px-5 py-10 text-center text-ink-light-muted dark:text-ink-dark-muted">
                    No worksheets uploaded yet.
                  </td>
                </tr>
              ) : (
                worksheets.map(w => (
                  <tr key={w.id} className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors">
                    <td className="px-5 py-4">
                      <button
                        onClick={() => handleDownload(w)}
                        className="flex items-center gap-1.5 text-neon-blue hover:underline"
                      >
                        {w.originalFilename ?? '—'}
                        <IconDownload size={13} />
                      </button>
                    </td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{w.rowCount ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">{w.rowsProcessed ?? '—'}</td>
                    <td className="px-5 py-4 text-ink-light-primary dark:text-ink-dark-primary">
                      {w.rowCount != null && w.rowsProcessed != null ? w.rowCount - w.rowsProcessed : '—'}
                    </td>
                    <td className="px-5 py-4">
                      {w.status
                        ? <span className={STATUS_CONFIG[w.status].colorClass}>{STATUS_CONFIG[w.status].label}</span>
                        : '—'}
                    </td>
                    <td className="px-5 py-4 text-ink-light-muted dark:text-ink-dark-muted">
                      {w.createdAt ? formatDateTime(w.createdAt) : '—'}
                    </td>
                    <td className="px-5 py-4">
                      <button
                        onClick={() => onOpenRows(w)}
                        className="flex items-center gap-1.5 text-neon-blue hover:underline"
                      >
                        <IconEye size={13} />
                        View Rows
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>
      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}
