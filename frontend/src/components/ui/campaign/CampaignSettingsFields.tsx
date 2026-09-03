import { useEffect, useState } from 'react'
import type { LinkedEntity } from '../../../types'
import { labelClass, inputClass, errorClass } from './campaignFormConstants'
import { LinkedEntitiesTable } from './LinkedEntitiesTable'
import { useConnections } from '../../../hooks/useConnections'
import { ToggleSwitch } from '../ToggleSwitch'
import { fetchStepConfig, type CampaignStepDto } from '../../../api/campaignApi'
import { AdditionalRewardFields } from './AdditionalRewardFields'

interface FormSlice {
  totalSlots: string
  returnWindowDays: string
  openToAll: boolean
  assignees: LinkedEntity[]
  requiredSteps: string[]
  additionalRewardType: string
  additionalRewardCashbackRupees: string
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: unknown) => void
  readOnly?: boolean
}

export function CampaignSettingsFields({ form, errors, set, readOnly }: Props) {
  const { connections, loading } = useConnections(!readOnly)
  const [selectableSteps, setSelectableSteps] = useState<CampaignStepDto[]>([])

  useEffect(() => {
    fetchStepConfig().then(setSelectableSteps)
  }, [])

  function handleOpenToAllToggle(next: boolean) {
    set('openToAll', next)
    set('assignees', next
      ? connections.map(c => ({ id: c.id, name: c.name, slotsAvailable: 0, commissionOffered: 0 }))
      : []
    )
  }

  function handleStepToggle(type: string, checked: boolean) {
    set('requiredSteps', checked
      ? [...form.requiredSteps, type]
      : form.requiredSteps.filter(t => t !== type))
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

      <div>
        <label className={labelClass}>Required Screenshots</label>
        <div className="space-y-2">
          {selectableSteps.map(step => (
            <label
              key={step.type}
              className="flex items-center gap-2 text-xs text-ink-light-primary dark:text-ink-dark-primary"
            >
              <input
                type="checkbox"
                checked={form.requiredSteps.includes(step.type)}
                disabled={readOnly || step.type === 'ORDER'}
                onChange={e => handleStepToggle(step.type, e.target.checked)}
              />
              {step.label}
            </label>
          ))}
        </div>
      </div>

      <AdditionalRewardFields form={form} errors={errors} set={set} readOnly={readOnly} />

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
        totalSlots={parseInt(form.totalSlots, 10) || 0}
      />
    </section>
  )
}
