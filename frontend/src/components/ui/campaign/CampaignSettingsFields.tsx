import type { LinkedEntity } from '../../../types'
import { labelClass, inputClass, errorClass } from './campaignFormConstants'
import { LinkedEntitiesTable } from './LinkedEntitiesTable'
import { RupeeInput } from '../RupeeInput'

interface FormSlice {
  totalSlots: string
  returnWindowDays: string
  openToAll: boolean
  assignees: LinkedEntity[]
  commissionToAllRupees: string
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: keyof FormSlice, value: unknown) => void
  readOnly?: boolean
}

export function CampaignSettingsFields({ form, errors, set, readOnly }: Props) {

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
          onWheel={e => e.currentTarget.blur()}  //prevent number input scroll changes
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
          onWheel={e => e.currentTarget.blur()}  //prevent number input scroll changes
          />
          {errors.returnWindowDays && <p className={errorClass}>{errors.returnWindowDays}</p>}
        </div>
      </div>

      <div className="flex items-center gap-3">
        <button
          type="button"
          role="switch"
          aria-checked={form.openToAll}
          onClick={() => !readOnly && set('openToAll', !form.openToAll)}
          className={[
            'relative inline-flex h-5 w-9 flex-shrink-0 rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none',
            readOnly ? 'cursor-not-allowed opacity-60' : 'cursor-pointer focus:ring-2 focus:ring-neon-blue/40',
            form.openToAll ? 'bg-neon-blue' : 'bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border',
          ].join(' ')}
        >
          <span
            className={[
              'pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200',
              form.openToAll ? 'translate-x-4' : 'translate-x-0',
            ].join(' ')}
          />
        </button>
        <span className="text-xs text-ink-light-primary dark:text-ink-dark-primary font-medium">Open to All</span>
        <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted">
          {form.openToAll ? 'Any agency can participate' : 'Restricted to allowed agencies'}
        </span>
      </div>

      {form.openToAll && (
        <div>
          <label className={labelClass}>Commission Offered</label>
          <RupeeInput
            value={form.commissionToAllRupees}
            onChange={v => set('commissionToAllRupees', v)}
            className={inputClass}
            disabled={readOnly}
          />
        </div>
      )}

      {!form.openToAll && (
        <LinkedEntitiesTable
          entities={form.assignees}
          onChange={v => set('assignees', v)}
          readOnly={readOnly}
        />
      )}
    </section>
  )
}
