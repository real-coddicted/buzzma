import { useState, useEffect } from 'react'
import type { AssignmentItem } from '../../../types/AssignmentTypes'
import { fetchUnpublishedAssignments } from '../../../api/assignmentApi'
import { AssignmentListView } from './AssignmentListView'

export function UnpublishedAssignments() {
  const [items, setItems] = useState<AssignmentItem[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchUnpublishedAssignments().then(data => {
      setItems(data)
      setLoading(false)
    })
  }, [])

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-ink-light-muted dark:text-ink-dark-muted text-sm">Loading…</p>
      </div>
    )
  }

  return <AssignmentListView items={items} />
}
