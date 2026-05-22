import { useState, useEffect, useMemo, useCallback } from 'react'
import { ConnectionsHeader } from '../components/ui/connections/ConnectionsHeader'
import { ConnectionsGrid } from '../components/ui/connections/ConnectionsGrid'
import type { ConnectionStatus, ConnectionFilterOption } from '../components/ui/connections/ConnectionsGrid'
import type { Connection } from '../types/ConnectionTypes'
import {
  fetchConnections,
  fetchConnectionSummary,
  fetchInviteCode,
  actionConnection,
  deleteConnection,
} from '../api/connectionApi'
import type { ConnectionSummary } from '../api/connectionApi'
import { InviteModal } from '../components/ui/connections/InviteModal'
import { Toast } from '../components/ui/Toast'
import { ConfirmModal } from '../components/ui/ConfirmModal'

const statusFilters: ConnectionFilterOption[] = [
  { value: 'all',       label: 'All'       },
  { value: 'connected', label: 'Connected' },
  { value: 'pending',   label: 'Pending'   },
  { value: 'rejected',   label: 'Rejected'   },
]

interface ToastState {
  message: string
  type: 'success' | 'error'
}

type ConfirmKind = 'reject' | 'delete'

interface ConfirmState {
  connection: Connection
  kind: ConfirmKind
}

const confirmCopy: Record<ConfirmKind, { title: string; verb: string; confirmLabel: string }> = {
  reject: { title: 'Reject connection', verb: 'reject the connection request from', confirmLabel: 'Reject' },
  delete: { title: 'Delete connection', verb: 'delete your connection with',        confirmLabel: 'Delete' },
}

export function Connections() {
  const [connections, setConnections] = useState<Connection[]>([])
  const [summary, setSummary]         = useState<ConnectionSummary>({ total: 0, connectedCount: 0, pendingCount: 0 })
  const [loading, setLoading]         = useState(true)

  const [search, setSearch]             = useState('')
  const [statusFilter, setStatusFilter] = useState<ConnectionStatus | 'all'>('connected')

  const [inviteCode, setInviteCode]     = useState<string | null>(null)
  const [inviteLoading, setInviteLoading] = useState(false)
  const [showInvite, setShowInvite]     = useState(false)

  const [toast, setToast]               = useState<ToastState | null>(null)
  const [actioningId, setActioningId]   = useState<string | null>(null)
  const [confirm, setConfirm]           = useState<ConfirmState | null>(null)

  function handleInvite() {
    setShowInvite(true)
    setInviteLoading(true)
    fetchInviteCode()
      .then(code => {
        setInviteCode(code)
        setInviteLoading(false)
      })
      .catch((err: unknown) => {
        setShowInvite(false)
        setInviteLoading(false)
        setToast({
          message: err instanceof Error ? err.message : 'Failed to generate invite code.',
          type: 'error',
        })
      })
  }

  const loadConnections = useCallback(() => {
    setLoading(true)
    fetchConnections(statusFilter)
      .then(list => setConnections(list))
      .catch((err: unknown) => {
        setConnections([])
        setToast({ message: err instanceof Error ? err.message : 'Failed to load connections.', type: 'error' })
      })
      .finally(() => setLoading(false))
  }, [statusFilter])

  const refreshSummary = useCallback(() => {
    // Summary counts are secondary — a failure here is swallowed rather than
    // surfaced, since loadConnections already reports load errors.
    fetchConnectionSummary().then(setSummary).catch(() => {})
  }, [])

  useEffect(() => { loadConnections() }, [loadConnections])
  useEffect(() => { refreshSummary() }, [refreshSummary])

  async function runAction(c: Connection, run: () => Promise<void>, successMessage: string) {
    setActioningId(c.id)
    try {
      await run()
      setToast({ message: successMessage, type: 'success' })
      loadConnections()
      refreshSummary()
    } catch (err: unknown) {
      setToast({ message: err instanceof Error ? err.message : 'Action failed.', type: 'error' })
    } finally {
      setActioningId(null)
    }
  }

  // Accept applies immediately; reject and delete are destructive, so they
  // open a confirmation modal first and run only once the user confirms.
  const handleAccept = (c: Connection) =>
    runAction(c, () => actionConnection(c.toUserId, 'ACTION_ACCEPT'), `Accepted connection with ${c.name}.`)

  const handleReject = (c: Connection) => setConfirm({ connection: c, kind: 'reject' })
  const handleDelete = (c: Connection) => setConfirm({ connection: c, kind: 'delete' })

  async function handleConfirm() {
    if (!confirm) return
    const { connection, kind } = confirm
    if (kind === 'reject') {
      await runAction(
        connection,
        () => actionConnection(connection.toUserId, 'ACTION_REJECT'),
        `Rejected connection with ${connection.name}.`,
      )
    } else {
      await runAction(
        connection,
        () => deleteConnection(connection.id),
        `Deleted connection with ${connection.name}.`,
      )
    }
    setConfirm(null)
  }

  const filtered = useMemo(() => {
    return connections.filter(c => {
      const matchesSearch =
        c.name.toLowerCase().includes(search.toLowerCase()) ||
        c.category.toLowerCase().includes(search.toLowerCase())
      return matchesSearch
    })
  }, [connections, search])

  return (
    <>
    {toast && (
      <Toast message={toast.message} type={toast.type} onDismiss={() => setToast(null)} />
    )}
    {confirm && (
      <ConfirmModal
        title={confirmCopy[confirm.kind].title}
        message={`Are you sure you want to ${confirmCopy[confirm.kind].verb} ${confirm.connection.name}? This can't be undone.`}
        confirmLabel={confirmCopy[confirm.kind].confirmLabel}
        tone="red"
        busy={actioningId === confirm.connection.id}
        onConfirm={handleConfirm}
        onCancel={() => setConfirm(null)}
      />
    )}
    {showInvite && (
      <InviteModal
        code={inviteCode}
        loading={inviteLoading}
        onClose={() => { setShowInvite(false); setInviteCode(null) }}
      />
    )}
    <div className="max-w-7xl mx-auto space-y-5">
      <ConnectionsHeader
        total={summary.total}
        connectedCount={summary.connectedCount}
        pendingCount={summary.pendingCount}
        onAddConnection={handleInvite}
      />

      <ConnectionsGrid
        rows={filtered}
        total={summary.total}
        loading={loading}
        search={search}
        onSearch={setSearch}
        statusFilter={statusFilter}
        onStatusFilter={setStatusFilter}
        filterOptions={statusFilters}
        actioningId={actioningId}
        onAccept={handleAccept}
        onReject={handleReject}
        onDelete={handleDelete}
      />
    </div>
    </>
  )
}