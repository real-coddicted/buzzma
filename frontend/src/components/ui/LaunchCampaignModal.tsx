import { useState, useEffect } from 'react'
import type { LinkedEntity } from '../../types'
import { Button } from './Button'
import { IconX, IconTrash, IconPlus } from './icons'
import { SearchEntityModal } from './SearchEntityModal'

interface LaunchCampaignModalProps {
  open: boolean
  campaignName: string
  linkedEntities: LinkedEntity[]
  availableEntities: LinkedEntity[]
  onClose: () => void
  onLaunch: () => void
}

export function LaunchCampaignModal({
  open,
  campaignName,
  linkedEntities,
  availableEntities,
  onClose,
  onLaunch,
}: LaunchCampaignModalProps) {
  const [entities, setEntities] = useState<LinkedEntity[]>([])
  const [searchModalOpen, setSearchModalOpen] = useState(false)

  useEffect(() => {
    if (open) {
      setEntities(linkedEntities)
    }
  }, [open, linkedEntities])

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape' && open && !searchModalOpen) {
        onClose()
      }
    }

    if (open && !searchModalOpen) {
      document.addEventListener('keydown', handleKeyDown)
      return () => document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open, searchModalOpen, onClose])

  if (!open) return null

  function handleSlotsChange(id: string, value: string) {
    const numValue = parseInt(value) || 0
    setEntities(entities.map(e =>
      e.id === id ? { ...e, slotsAvailable: numValue } : e
    ))
  }

  function handleCommissionChange(id: string, value: string) {
    const numValue = parseFloat(value) || 0
    setEntities(entities.map(e =>
      e.id === id ? { ...e, commissionOffered: numValue } : e
    ))
  }

  function handleRemoveEntity(id: string) {
    setEntities(entities.filter(e => e.id !== id))
  }

  function handleAddEntity() {
    setSearchModalOpen(true)
  }

  function handleAddEntityFromSearch(incoming: LinkedEntity[]) {
    const newEntities = incoming.filter(e => !entities.some(existing => existing.id === e.id))
    if (newEntities.length > 0) {
      setEntities([
        ...entities,
        ...newEntities.map(e => ({ ...e, slotsAvailable: 0, commissionOffered: e.commissionOffered })),
      ])
    }
    setSearchModalOpen(false)
  }

  const totalSlots = entities.reduce((sum, e) => sum + e.slotsAvailable, 0)
  const totalCommission = entities.reduce(
    (sum, e) => sum + e.slotsAvailable * e.commissionOffered,
    0
  )

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div className="bg-surface-light-card dark:bg-surface-dark-card rounded-xl border border-surface-light-border dark:border-surface-dark-border w-full max-w-2xl max-h-[80vh] flex flex-col">
        <div className="flex items-center justify-between p-6 border-b border-surface-light-border dark:border-surface-dark-border">
          <div>
            <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary">
              Launch Campaign
            </h2>
            <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
              {campaignName}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
          >
            <IconX size={18} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">
          <div className="p-6">
            <div className="flex items-center justify-between mb-4">
              <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary">
                Linked entities assigned to this campaign:
              </p>
              <button
                onClick={handleAddEntity}
                className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-neon-blue/10 text-neon-blue hover:bg-neon-blue/15 transition-colors text-xs font-semibold"
              >
                <IconPlus size={14} />
                Add Entity
              </button>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover">
                    <th className="text-left px-4 py-3 font-semibold text-xs uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted">
                      Entity Name
                    </th>
                    <th className="text-left px-4 py-3 font-semibold text-xs uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted">
                      Slots Assigned
                    </th>
                    <th className="text-left px-4 py-3 font-semibold text-xs uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted">
                      Commission offered
                    </th>
                    <th className="text-right px-4 py-3 font-semibold text-xs uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted">
                      Action
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-light-border dark:divide-surface-dark-border">
                  {entities.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-4 py-8 text-center text-ink-light-muted dark:text-ink-dark-muted">
                        No linked entities. Add one to get started.
                      </td>
                    </tr>
                  ) : (
                    entities.map(entity => (
                      <tr
                        key={entity.id}
                        className="hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
                      >
                        <td className="px-4 py-4">
                          <span className="font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                            {entity.name}
                          </span>
                        </td>
                        <td className="px-4 py-4">
                          <input
                            type="number"
                            value={entity.slotsAvailable}
                            onChange={e => handleSlotsChange(entity.id, e.target.value)}
                            min="0"
                            className="w-24 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg px-2 py-1 text-ink-light-primary dark:text-ink-dark-primary text-sm outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all"
                          />
                        </td>
                        <td className="px-4 py-4">
                          <div className="flex items-center gap-1">
                            <span className="text-ink-light-muted dark:text-ink-dark-muted">₹</span>
                            <input
                              type="number"
                              value={entity.commissionOffered}
                              onChange={e => handleCommissionChange(entity.id, e.target.value)}
                              step="1"
                              min="0"
                              className="w-20 bg-transparent border border-surface-light-border dark:border-surface-dark-border rounded-lg px-2 py-1 text-ink-light-primary dark:text-ink-dark-primary text-sm outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all"
                            />
                          </div>
                        </td>
                        <td className="px-4 py-4 text-right">
                          <button
                            onClick={() => handleRemoveEntity(entity.id)}
                            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-neon-red hover:bg-neon-red/10 transition-colors"
                            title="Remove entity"
                          >
                            <IconTrash size={16} />
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {entities.length > 0 && (
              <div className="mt-6 p-4 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border">
                <div className="grid grid-cols-3 gap-4 text-center">
                  <div>
                    <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider font-semibold">
                      Total Entities
                    </p>
                    <p className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary mt-1">
                      {entities.length}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider font-semibold">
                      Total Slots
                    </p>
                    <p className="text-lg font-bold text-neon-cyan mt-1">
                      {totalSlots}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wider font-semibold">
                      Total Commission
                    </p>
                    <p className="text-lg font-bold text-neon-green mt-1">
                      ₹{totalCommission.toLocaleString()}
                    </p>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 p-6 border-t border-surface-light-border dark:border-surface-dark-border">
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="primary" onClick={onLaunch}>
            Launch Campaign
          </Button>
        </div>
      </div>

      <SearchEntityModal
        open={searchModalOpen}
        availableEntities={availableEntities}
        assignedEntityIds={entities.map(e => e.id)}
        onClose={() => setSearchModalOpen(false)}
        onConfirm={handleAddEntityFromSearch}
      />
    </div>
  )
}
