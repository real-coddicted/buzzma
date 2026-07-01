import { useState, useEffect } from 'react'
import type { AssignmentSummary } from '../../../types/AssignmentTypes'
import { fetchPublishedAssignments } from '../../../api/assignmentApi'
import { AssignmentListView } from './AssignmentListView'
import { Loading } from '../Loading'
import { Toast } from '../Toast'

interface PublishedAssignmentsProps {
  onSelect: (item: AssignmentSummary) => void
}

export function PublishedAssignments({ onSelect }: PublishedAssignmentsProps) {
  const [items, setItems]     = useState<AssignmentSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState<string | null>(null)

  useEffect(() => {
    fetchPublishedAssignments()
      .then(data => { setItems(data); setLoading(false) })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to load published assignments.')
        setLoading(false)
      })
  }, [])

  return (
    <>
      {error && <Toast type="error" message={error} onDismiss={() => setError(null)} />}
      <div className="p-4 max-h-[560px] overflow-y-auto">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loading size={32} />
          </div>
        ) : (
          <AssignmentListView items={items} onSelect={onSelect} />
        )}
      </div>
    </>
  )
}
