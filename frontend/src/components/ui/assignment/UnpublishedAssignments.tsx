import { useState, useEffect, useMemo } from 'react'
import type { AssignmentItem } from '../../../types/AssignmentTypes'
import { fetchUnpublishedAssignments } from '../../../api/assignmentApi'
import { AssignmentFilterBar } from './AssignmentFilterBar'
import type { AssignmentTypeFilter } from './AssignmentFilterBar'
import { AssignmentListView } from './AssignmentListView'
import { Loading } from '../Loading'
import { Toast } from '../Toast'

interface UnpublishedAssignmentsProps {
  onSelect: (item: AssignmentItem) => void
}

export function UnpublishedAssignments({ onSelect }: UnpublishedAssignmentsProps) {
  const [items, setItems]           = useState<AssignmentItem[]>([])
  const [loading, setLoading]       = useState(true)
  const [error, setError]           = useState<string | null>(null)
  const [search, setSearch]         = useState('')
  const [typeFilter, setTypeFilter] = useState<AssignmentTypeFilter>('all')

  useEffect(() => {
    fetchUnpublishedAssignments()
      .then(data => { setItems(data); setLoading(false) })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Failed to load assignments.')
        setLoading(false)
      })
  }, [])

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
      <div className="p-4 max-h-[560px] overflow-y-auto">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loading size={32} />
          </div>
        ) : (
          <AssignmentListView items={filtered} onSelect={onSelect} />
        )}
      </div>
    </>
  )
}
