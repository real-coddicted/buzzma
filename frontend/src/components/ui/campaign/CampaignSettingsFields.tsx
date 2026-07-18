import type { LinkedEntity } from '../../../types'
import { labelClass, inputClass, errorClass } from './campaignFormConstants'
import { LinkedEntitiesTable } from './LinkedEntitiesTable'
import { useConnections } from '../../../hooks/useConnections'
import { ToggleSwitch } from '../ToggleSwitch'

interface FormSlice {
  totalSlots: string
  returnWindowDays: string
  openToAll: boolean
  assignees: LinkedEntity[]
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: unknown) => void
  readOnly?: boolean
}

export function CampaignSettingsFields({ form, errors, set, readOnly }: Props) {
  const { connections, loading } = useConnections(!readOnly)

  function handleOpenToAllToggle(next: boolean) {
    set('openToAll', next)
    set('assignees', next
      ? connections.map(c => ({ id: c.id, name: c.name, slotsAvailable: 0, commissionOffered: 0 }))
      : []
    )
  }

  return (
    <section className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-4">
      <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-orange">Campaign Settings</h3>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className={labelClass}>Total Slots</label>
          <input className={[inputClass, '[appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none'].join(' ')}
          type="number"
          placeholder="e.g. 100"
          value={form.totalSlots}
          onChange={e => set('totalSlots', e.target.value)}
          disabled={readOnly}
          onWheel={e => e.currentTarget.blur()}
          />
          {errors.totalSlots && <p className={errorClass}>{errors.totalSlots}</p>}
        </div>
        <div>
          <label className={labelClass}>Return Window (days)</label>
          <input className={[inputClass, '[appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none'].join(' ')}
          type="number"
          min="0"
          placeholder="e.g. 30"
          value={form.returnWindowDays}
          onChange={e => set('returnWindowDays', e.target.value)}
          disabled={readOnly}
          onWheel={e => e.currentTarget.blur()}
          />
          {errors.returnWindowDays && <p className={errorClass}>{errors.returnWindowDays}</p>}
        </div>
      </div>

      <ToggleSwitch
        checked={form.openToAll}
        onChange={handleOpenToAllToggle}
        disabled={readOnly || loading}
        label="Open to All"
        hint={form.openToAll ? 'Any agency can participate' : 'Restricted to allowed agencies'}
      />

      <LinkedEntitiesTable
        entities={form.assignees}
        connections={connections}
        onChange={assignees => set('assignees', assignees)}
        openToAll={form.openToAll}
        readOnly={readOnly}
      />
    </section>
  )
}
