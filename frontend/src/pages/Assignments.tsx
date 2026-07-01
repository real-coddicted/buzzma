import { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Card } from '../components/ui/Card'
import { Loading } from '../components/ui/Loading'
import { Toast } from '../components/ui/Toast'
import { AssignmentTabs, type AssignmentTab } from '../components/ui/assignment/AssignmentTabs'
import { UnpublishedAssignments } from '../components/ui/assignment/UnpublishedAssignments'
import { PublishedAssignments } from '../components/ui/assignment/PublishedAssignments'
import { AssignmentDetail } from '../components/ui/assignment/AssignmentDetail'
import type { AssignmentItem, AssignmentSummary } from '../types/AssignmentTypes'
import { getAssignmentById } from '../api/assignmentApi'

export function Assignments() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const activeTab: AssignmentTab = (searchParams.get('tab') as AssignmentTab) ?? 'unpublished'
  const assignmentId = searchParams.get('id')
  const isDetailView = searchParams.get('view') === 'detail' && !!assignmentId

  const [assignment, setAssignment] = useState<AssignmentItem | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!assignmentId) {
      setAssignment(null)
      return
    }
    let cancelled = false
    setLoading(true)
    getAssignmentById(assignmentId)
      .then(a => { if (!cancelled) setAssignment(a) })
      .catch(() => {
        if (!cancelled) {
          setError('Failed to load assignment.')
          setSearchParams({ tab: activeTab })
        }
      })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [assignmentId, activeTab, setSearchParams])

  function handleSelect(item: AssignmentSummary) {
    setSearchParams({ tab: activeTab, view: 'detail', id: item.id })
  }

  if (isDetailView && loading) {
    return (
      <div className="flex justify-center py-20 text-ink-light-muted dark:text-ink-dark-muted">
        <Loading size={32} />
      </div>
    )
  }

  if (assignment) {
    return (
      <AssignmentDetail
        item={assignment}
        onBack={() => navigate(-1)}
        readOnly={activeTab === 'published'}
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

      <AssignmentTabs value={activeTab} onChange={tab => setSearchParams({ tab })} />

      <Card padded={false}>
        {activeTab === 'unpublished' && (
          <UnpublishedAssignments onSelect={handleSelect} />
        )}
        {activeTab === 'published' && (
          <PublishedAssignments onSelect={handleSelect} />
        )}
      </Card>

      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}
    </div>
  )
}