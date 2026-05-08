import { useState, useEffect, useMemo } from 'react'
import type { AssignmentItem } from '../../../types/AssignmentTypes'
import { fetchUnpublishedAssignments } from '../../../api/assignmentApi'
import { AssignmentFilterBar } from './AssignmentFilterBar'
import type { AssignmentTypeFilter } from './AssignmentFilterBar'
import { AssignmentListView } from './AssignmentListView'

export function UnpublishedAssignments() {
  const [items, setItems]       = useState<AssignmentItem[]>([])
  const [loading, setLoading]   = useState(true)
  const [search, setSearch]     = useState('')
  const [typeFilter, setTypeFilter] = useState<AssignmentTypeFilter>('all')

  useEffect(() => {
    fetchUnpublishedAssignments().then(data => {
      setItems(data)
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
      <div className="p-4 border-b border-surface-light-border dark:border-surface-dark-border">
        <AssignmentFilterBar
          search={search}
          onSearchChange={setSearch}
          typeFilter={typeFilter}
          onTypeChange={setTypeFilter}
        />
      </div>
      <div className="p-4">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <p className="text-ink-light-muted dark:text-ink-dark-muted text-sm">Loading…</p>
          </div>
        ) : (
          <AssignmentListView items={filtered} />
        )}
      </div>
    </>
  )
}
