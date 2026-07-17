import { useState, useEffect, useCallback } from 'react'
import type { AssignmentSummary } from '../../../types/AssignmentTypes'
import { fetchPublishedAssignments } from '../../../api/assignmentApi'
import { AssignmentListView } from './AssignmentListView'
import { Loading } from '../Loading'
import { Toast } from '../Toast'
import { PaginationToolbar } from '../PaginationToolbar'
import { useSSE } from '../../../hooks/useSSE'

interface PublishedAssignmentsProps {
  onSelect: (item: AssignmentSummary) => void
}

export function PublishedAssignments({ onSelect }: PublishedAssignmentsProps) {
  const [items, setItems]     = useState<AssignmentSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState<string | null>(null)
  const [currentPage, setCurrentPage] = useState(1)
  const [totalPages, setTotalPages]   = useState(1)

  const loadPublishedAssignments = useCallback((page = 1) => {
    setLoading(true)
    fetchPublishedAssignments(page - 1)
      .then(data => {
        setItems(data.items)
        setTotalPages(data.totalPages)
        setCurrentPage(page)
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to load published assignments.')
      })
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { loadPublishedAssignments() }, [loadPublishedAssignments])

  useSSE('EVENT_TYPE_REFRESH', () => loadPublishedAssignments(), 'assignments')

  return (
    <>
      {error && <Toast type="error" message={error} onDismiss={() => setError(null)} />}
      <div className="p-4 max-h-[600px] overflow-y-auto">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loading size={32} />
          </div>
        ) : (
          <AssignmentListView items={items} onSelect={onSelect} />
        )}
      </div>
      <PaginationToolbar
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={loadPublishedAssignments}
        disabled={loading}
      />
    </>
  )
}
