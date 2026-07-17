import { useState, useEffect, useMemo, useCallback } from 'react'
import type { AssignmentSummary } from '../../../types/AssignmentTypes'
import { fetchUnpublishedAssignments } from '../../../api/assignmentApi'
import { AssignmentFilterBar } from './AssignmentFilterBar'
import type { AssignmentTypeFilter } from './AssignmentFilterBar'
import { AssignmentListView } from './AssignmentListView'
import { Loading } from '../Loading'
import { Toast } from '../Toast'
import { PaginationToolbar } from '../PaginationToolbar'
import { useSSE } from '../../../hooks/useSSE'

interface UnpublishedAssignmentsProps {
  onSelect: (item: AssignmentSummary) => void
}

export function UnpublishedAssignments({ onSelect }: UnpublishedAssignmentsProps) {
  const [items, setItems]           = useState<AssignmentSummary[]>([])
  const [loading, setLoading]       = useState(true)
  const [error, setError]           = useState<string | null>(null)
  const [search, setSearch]         = useState('')
  const [typeFilter, setTypeFilter] = useState<AssignmentTypeFilter>('all')
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages]   = useState(1)

  const loadUnpublishedAssignments = useCallback((page = 1) => {
    setLoading(true)
    fetchUnpublishedAssignments(page - 1)
      .then(data => {
        setItems(data.items)
        setTotalPages(data.totalPages)
        setCurrentPage(page)
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to load assignments.')
      })
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { loadUnpublishedAssignments() }, [loadUnpublishedAssignments])

  useSSE('EVENT_TYPE_REFRESH', () => loadUnpublishedAssignments(), 'assignments')

  const filtered = useMemo(() => items.filter(item => {
    const matchesType   = typeFilter === 'all' || item.dealType === typeFilter
    const matchesSearch = item.productName.toLowerCase().includes(search.toLowerCase())
    return matchesType && matchesSearch
  }), [items, search, typeFilter])

  return (
    <>
      {error && <Toast type="error" message={error} onDismiss={() => setError(null)} />}
      <div className="p-4 border-b border-surface-light-border dark:border-surface-dark-border">
        <AssignmentFilterBar
          search={search}
          onSearchChange={setSearch}
          typeFilter={typeFilter}
          onTypeChange={setTypeFilter}
        />
      </div>
      <div className="p-4 max-h-[600px] overflow-y-auto">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loading size={32} />
          </div>
        ) : (
          <AssignmentListView items={filtered} onSelect={onSelect} />
        )}
      </div>
      <PaginationToolbar
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={loadUnpublishedAssignments}
        disabled={loading}
      />
    </>
  )
}
