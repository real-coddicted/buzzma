import { useState, type FormEvent } from 'react'
import { Button } from '../Button'
import { Toast } from '../Toast'
import { IconPlus, IconChevronRight, IconCheck, IconPlay } from '../icons'
import type { CampaignRequestDto } from '../../../types'
import { EMPTY_FORM, validateCampaignForm, type CampaignForm } from './campaignFormConstants'
import { CampaignInfoFields } from './CampaignInfoFields'
import { CampaignSettingsFields } from './CampaignSettingsFields'

interface Props {
  onBack: () => void
  onSubmit: (dto: CampaignRequestDto) => Promise<void>
  onLaunch?: () => Promise<void>
  initialForm?: CampaignForm
}

function rupeesToPaise(val: string): number {
  return Math.round(parseFloat(val) * 100)
}

export function NewCampaignPage({ onBack, onSubmit, onLaunch, initialForm }: Props) {
  const isEdit = !!initialForm
  const [form, setForm] = useState<CampaignForm>(initialForm ?? EMPTY_FORM)
  const [errors, setErrors] = useState<Partial<Record<string, string>>>({})
  const [loading, setLoading] = useState(false)
  const [launching, setLaunching] = useState(false)
  const [toastError, setToastError] = useState<string | null>(null)

  function set(field: keyof typeof EMPTY_FORM, value: unknown) {
    setForm(prev => ({ ...prev, [field]: value }))
    setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  function validate(): boolean {
    const e = validateCampaignForm(form)
    setErrors(e)
    return Object.keys(e).length === 0
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    setToastError(null)

    const dto: CampaignRequestDto = {
      title: form.title.trim(),
      platform: form.platform,
      productBrandName: form.productBrandName.trim(),
      productName: form.productName.trim(),
      productImageUrl: form.productImageUrl.trim(),
      productUrl: form.productUrl.trim(),
      sellerName: form.sellerName.trim() || null,
      originalPricePaise: rupeesToPaise(form.originalPriceRupees),
      campaignPricePaise: rupeesToPaise(form.campaignPriceRupees),
      commissionOfferedPaise: rupeesToPaise(form.commissionRupees),
      returnWindowDays: form.returnWindowDays !== '' ? parseInt(form.returnWindowDays, 10) : null,
      campaignType: form.campaignType !== '' ? form.campaignType : null,
      totalSlots: form.totalSlots !== '' ? parseInt(form.totalSlots, 10) : null,
      assignees: form.openToAll ? null : form.assignees.length > 0 ? form.assignees : null,
      openToAll: form.openToAll,
      termsAndConditions: form.termsAndConditions.trim() || null,
      startDate: form.startDate || null,
      endDate: form.endDate || null,
    }

    try {
      await onSubmit(dto)
    } catch (err) {
      setToastError(err instanceof Error ? err.message : 'An unexpected error occurred.')
    } finally {
      setLoading(false)
    }
  }

  async function handleLaunch() {
    if (!onLaunch) return
    setLaunching(true)
    setToastError(null)
    try {
      await onLaunch()
    } catch (err) {
      setToastError(err instanceof Error ? err.message : 'Failed to launch campaign.')
    } finally {
      setLaunching(false)
    }
  }

  return (
    <div className="max-w-7xl mx-auto space-y-5">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-xs text-ink-light-muted dark:text-ink-dark-muted">
        <button onClick={onBack} className="hover:text-neon-blue transition-colors">
          Campaigns
        </button>
        <IconChevronRight size={12} />
        <span className="text-ink-light-primary dark:text-ink-dark-primary font-medium">
          {isEdit ? 'Edit Campaign' : 'New Campaign'}
        </span>
      </div>

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            {isEdit ? 'Edit Campaign' : 'New Campaign'}
          </h1>
          <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
            {isEdit ? 'Update the details for this campaign.' : 'Fill in the details to create a new campaign.'}
          </p>
        </div>
      </div>

      <form id="new-campaign-form" onSubmit={handleSubmit} noValidate>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <CampaignInfoFields form={form} errors={errors} set={set} />

          <CampaignSettingsFields
            form={form}
            errors={errors}
            set={set}
          />
        </div>

        <section className="mt-6 rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card p-5 space-y-2">
          <h3 className="text-[11px] font-bold uppercase tracking-widest text-neon-purple">Terms & Conditions</h3>
          <textarea
            rows={5}
            placeholder="Enter campaign terms and conditions…"
            value={form.termsAndConditions}
            onChange={e => set('termsAndConditions', e.target.value)}
            className="w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover border-surface-light-border dark:border-surface-dark-border text-xs text-ink-light-primary dark:text-ink-dark-primary placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted px-3 py-2 outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all resize-none"
          />
        </section>

        {/* Footer actions */}
        <div className="flex items-center justify-end gap-2 mt-6">
          <Button variant="secondary" size="sm" type="button" onClick={onBack} disabled={loading || launching}>
            Cancel
          </Button>
          {isEdit ? (
            <>
              <Button
                type="submit"
                variant="secondary"
                size="sm"
                leftIcon={<IconCheck size={13} />}
                loading={loading}
                disabled={launching}
              >
                Update Campaign
              </Button>
              <Button
                type="button"
                variant="primary"
                size="sm"
                leftIcon={<IconPlay size={13} />}
                loading={launching}
                disabled={loading}
                onClick={handleLaunch}
              >
                Launch Campaign
              </Button>
            </>
          ) : (
            <Button
              type="submit"
              variant="primary"
              size="sm"
              leftIcon={<IconPlus size={13} />}
              loading={loading}
            >
              Create Campaign
            </Button>
          )}
        </div>
      </form>

      {toastError && (
        <Toast message={toastError} type="error" onDismiss={() => setToastError(null)} />
      )}
    </div>
  )
}
