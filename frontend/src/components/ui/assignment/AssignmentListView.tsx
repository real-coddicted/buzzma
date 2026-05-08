import type { AssignmentItem } from '../../../types/AssignmentTypes'
import { AssignmentListItem } from './AssignmentListItem'

interface AssignmentListViewProps {
  items: AssignmentItem[]
  onSelect?: (item: AssignmentItem) => void
}

export function AssignmentListView({ items, onSelect }: AssignmentListViewProps) {
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
        <AssignmentListItem key={item.id} item={item} onClick={onSelect ? () => onSelect(item) : undefined} />
      ))}
    </div>
  )
}
