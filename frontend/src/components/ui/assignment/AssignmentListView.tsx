import type { AssignmentItem } from '../../../types/AssignmentTypes'
import { AssignmentListItem } from './AssignmentListItem'

interface AssignmentListViewProps {
  items: AssignmentItem[]
}

export function AssignmentListView({ items }: AssignmentListViewProps) {
  if (items.length === 0) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-ink-light-muted dark:text-ink-dark-muted text-sm">No assignments found.</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-3">
      {items.map(item => (
        <AssignmentListItem key={item.id} item={item} />
      ))}
    </div>
  )
}
