import { useState, useEffect } from 'react'
import type { LinkedEntity } from '../../../types'
import { Button } from '../Button'
import { IconX } from '../icons'
import { LinkedEntitiesTable } from './LinkedEntitiesTable'

interface LaunchCampaignModalProps {
  open: boolean
  campaignName: string
  linkedEntities: LinkedEntity[]
  onClose: () => void
  onLaunch: () => void
}

export function LaunchCampaignModal({
  open,
  campaignName,
  linkedEntities,
  onClose,
  onLaunch,
}: LaunchCampaignModalProps) {
  const [entities, setEntities] = useState<LinkedEntity[]>([])

  useEffect(() => {
    if (open) setEntities(linkedEntities)
  }, [open, linkedEntities])

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    if (open) {
      document.addEventListener('keydown', handleKeyDown)
      return () => document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open, onClose])

  if (!open) return null

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

        <div className="flex-1 overflow-y-auto p-6">
          <LinkedEntitiesTable
            entities={entities}
            onChange={setEntities}
            readOnly
          />
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
    </div>
  )
}
