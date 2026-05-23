import { useState } from 'react'
import { Card } from '../components/ui/Card'
import { AssignmentTabs, type AssignmentTab } from '../components/ui/assignment/AssignmentTabs'
import { UnpublishedAssignments } from '../components/ui/assignment/UnpublishedAssignments'
import { PublishedAssignments } from '../components/ui/assignment/PublishedAssignments'
import { AssignmentDetail } from '../components/ui/assignment/AssignmentDetail'
import type { AssignmentItem } from '../types/AssignmentTypes'

export function Assignments() {
  const [activeTab, setActiveTab] = useState<AssignmentTab>('unpublished')
  const [selected, setSelected]   = useState<{ item: AssignmentItem; published: boolean } | null>(null)

  if (selected) {
    return (
      <AssignmentDetail
        item={selected.item}
        onBack={() => setSelected(null)}
        readOnly={selected.published}
      />
    )
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Assignments
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          {activeTab === 'unpublished' ? 'Unpublished assignments' : 'Published assignments'}
        </p>
      </div>

      <AssignmentTabs value={activeTab} onChange={setActiveTab} />

      <Card padded={false}>
        {activeTab === 'unpublished' && (
          <UnpublishedAssignments onSelect={item => setSelected({ item, published: false })} />
        )}
        {activeTab === 'published' && (
          <PublishedAssignments onSelect={item => setSelected({ item, published: true })} />
        )}
      </Card>
    </div>
  )
}
