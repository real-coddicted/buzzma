import { labelClass, inputClass, errorClass } from './campaignFormConstants'

interface FormSlice {
  additionalRewardType: string
  additionalRewardCashbackRupees: string
}

interface Props {
  form: FormSlice
  errors: Partial<Record<string, string>>
  set: (field: 'additionalRewardType' | 'additionalRewardCashbackRupees', value: unknown) => void
  readOnly?: boolean
}

export function AdditionalRewardFields({ form, errors, set, readOnly }: Props) {
  return (
    <div className="grid grid-cols-2 gap-4">
      <div>
        <label className={labelClass}>Additional Reward</label>
        <select
          className={inputClass}
          value={form.additionalRewardType}
          onChange={e => {
            set('additionalRewardType', e.target.value)
            if (e.target.value === '') set('additionalRewardCashbackRupees', '')
          }}
          disabled={readOnly}
        >
          <option value="">— None —</option>
          <option value="CASHBACK">Cashback</option>
        </select>
      </div>
      {form.additionalRewardType === 'CASHBACK' && (
        <div>
          <label className={labelClass}>Cashback Amount (₹)</label>
          <input
            className={inputClass}
            type="number"
            min="0"
            placeholder="e.g. 400"
            value={form.additionalRewardCashbackRupees}
            onChange={e => set('additionalRewardCashbackRupees', e.target.value)}
            disabled={readOnly}
          />
          {errors.additionalRewardCashbackRupees && (
            <p className={errorClass}>{errors.additionalRewardCashbackRupees}</p>
          )}
        </div>
      )}
    </div>
  )
}
