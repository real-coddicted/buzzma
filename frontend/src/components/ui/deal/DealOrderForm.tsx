import { useState } from 'react'
import { IconCalendar } from '../icons'
import { ScreenshotUpload } from './ScreenshotUpload'
import type { ExtractionResponse } from '../../../api/extractionApi'
import { submitClaim, updateOrderScreenshot } from '../../../api/claimApi'
import type { components } from '../../../types/api'
import { ScoreBadge } from '../../utils/ScoreBadge'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ScoredValue = components['schemas']['ScoredValue']

interface ResubmitConfig {
  claimId: string
  screenshotId: string
  initialScreenshotUrl?: string
}

interface DealOrderFormProps {
  dealId: string
  campaignId: string
  onSuccess?: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimValues?: Partial<FormFields>
  resubmit?: ResubmitConfig
}

export interface FormFields {
  platform: string
  orderId: string
  amount: string
  productName: string
  sellerName: string
  orderDate: string
  accountName: string
}

const scoreKeyMap: Partial<Record<string, keyof FormFields>> = {
  platform:    'platform',
  productName: 'productName',
  sellerName:  'sellerName',
  orderDate:   'orderDate',
  amount:      'amount',
}

export function DealOrderForm({ dealId, campaignId, onSuccess, readOnly = false, claimValues, resubmit }: DealOrderFormProps) {
  const [fields, setFields] = useState<FormFields>(() => ({
    platform:    '',
    orderId:     '',
    amount:      '',
    productName: '',
    sellerName:  '',
    orderDate:   '',
    accountName: '',
    ...claimValues,
  }))
  const [screenshotFile, setScreenshotFile] = useState<File | null>(null)
  const [extractedDetails, setExtractedDetails] = useState<Record<string, ScoredValue>>({})
  const [overallScore, setOverallScore] = useState<number | null>(null)
  const [fieldScores, setFieldScores] = useState<Partial<Record<keyof FormFields, number>>>({})
  const [extractionErrors, setExtractionErrors] = useState<Partial<Record<keyof FormFields, string>>>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Maps ExtractionResult field names to FormFields keys
  const extractionFieldMap: Partial<Record<string, keyof FormFields>> = {
    orderId:     'orderId',
    amount:      'amount',
    productName: 'productName',
    sellerName:  'sellerName',
    orderDate:   'orderDate',
    orderedBy:   'accountName',
  }

  function set(key: keyof FormFields) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setFields(prev => ({ ...prev, [key]: e.target.value }))
      setExtractionErrors(prev => { const next = { ...prev }; delete next[key]; return next })
    }


  function handleExtraction(data: ExtractionResponse) {
    setFields(prev => ({
      ...prev,
      platform:    data.platform    || prev.platform,
      orderId:     data.orderId     ?? prev.orderId,
      amount:      data.amount != null ? String(data.amount) : prev.amount,
      productName: data.productName ?? prev.productName,
      sellerName:  data.sellerName  ?? prev.sellerName,
      orderDate:   data.orderDate   ?? prev.orderDate,
      accountName: data.orderedBy   ?? prev.accountName,
    }))

    const errors: Partial<Record<keyof FormFields, string>> = {}
    for (const ve of data.validationErrors ?? []) {
      if (!ve.field || !ve.message) continue
      const formKey = extractionFieldMap[ve.field]
      if (formKey) errors[formKey] = ve.message
    }
    setExtractionErrors(errors)

    const result = data.extractedResult ?? {}
    setExtractedDetails(result)
    setOverallScore(data.overallScore ?? null)

    const scores: Partial<Record<keyof FormFields, number>> = {}
    for (const [apiKey, formKey] of Object.entries(scoreKeyMap)) {
      const sv = result[apiKey]
      if (sv?.score != null) scores[formKey!] = sv.score
    }
    setFieldScores(scores)
  }

  const isValid =
    screenshotFile !== null &&
    Object.values(fields).every(v => v.trim() !== '')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!isValid || isSubmitting || !screenshotFile) return

    setIsSubmitting(true)
    setError(null)
    try {
      const claim = resubmit
        ? await updateOrderScreenshot(resubmit.claimId, resubmit.screenshotId, screenshotFile, {
            platform:    fields.platform,
            orderId:     fields.orderId,
            amount:      parseFloat(fields.amount),
            productName: fields.productName,
            sellerName:  fields.sellerName,
            orderDate:   fields.orderDate,
            accountName: fields.accountName,
          })
        : await submitClaim({
            campaignId,
            dealId,
            platform:        fields.platform,
            orderId:         fields.orderId,
            amount:          parseFloat(fields.amount),
            productName:     fields.productName,
            sellerName:      fields.sellerName,
            orderDate:       fields.orderDate,
            accountName:     fields.accountName,
            screenshot:      screenshotFile,
            extractedDetails,
            overallScore,
          })
      onSuccess?.(claim)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      {!readOnly && (
        <>
          <ScreenshotUpload
            label="Order Confirmation Screenshot"
            hint="Ensure the order ID, amount, and product name are clearly visible."
            campaignId={resubmit ? undefined : campaignId}
            onExtract={resubmit ? undefined : handleExtraction}
            onFileChange={setScreenshotFile}
            initialPreview={resubmit?.initialScreenshotUrl}
          />
          <Field
            as="select"
            label="Platform"
            placeholder="Select platform"
            value={fields.platform}
            onChange={set('platform')}
            score={fieldScores.platform}
            options={[
              { value: '', label: 'Select platform' },
              { value: 'PLATFORM_AMAZON', label: 'Amazon' },
              { value: 'PLATFORM_FLIPKART', label: 'Flipkart' },
              { value: 'PLATFORM_NYKAA', label: 'Nykaa' },
              { value: 'PLATFORM_MYNTRA', label: 'Myntra' },
            ]}
          />
        </>
      )}
      <Field label="Order ID"     placeholder="e.g. 403-1234567-8901234" value={fields.orderId}     onChange={set('orderId')}    error={extractionErrors.orderId}    readOnly={readOnly} />
      <Field label="Amount"       placeholder="e.g. 1499"                value={fields.amount}      onChange={set('amount')}      error={extractionErrors.amount}     score={fieldScores.amount}     readOnly={readOnly} />
      <Field label="Product Name" placeholder="Enter product name"        value={fields.productName} onChange={set('productName')} error={extractionErrors.productName} score={fieldScores.productName} readOnly={readOnly} />
      <Field label="Seller Name"  placeholder="Enter seller name"         value={fields.sellerName}  onChange={set('sellerName')}  error={extractionErrors.sellerName}  score={fieldScores.sellerName}  readOnly={readOnly} />

      <div>
        <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
          Order Date {!readOnly && <span className="text-neon-red">*</span>}
          {extractionErrors.orderDate && <span className="text-neon-red font-normal ml-2">{extractionErrors.orderDate}</span>}
          {!extractionErrors.orderDate && <ScoreBadge score={fieldScores.orderDate} />}
        </label>
        <div className="relative">
          <input
            type="date"
            value={fields.orderDate}
            onChange={set('orderDate')}
            disabled={readOnly}
            className="w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary dark:text-ink-dark-primary pl-9 pr-3 py-2 outline-none focus:border-neon-blue/50 transition-colors"
          />
          <IconCalendar size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink-light-muted dark:text-ink-dark-muted pointer-events-none" />
        </div>
      </div>

      <Field label="Account Name" placeholder="Name on your account" value={fields.accountName} onChange={set('accountName')} error={extractionErrors.accountName} readOnly={readOnly} />

      {!readOnly && error && (
        <p className="text-[11px] text-neon-red">{error}</p>
      )}

      {!readOnly && (
        <button
          type="submit"
          disabled={!isValid || isSubmitting}
          className="w-full py-2.5 rounded-lg bg-neon-blue text-surface-dark-base text-sm font-semibold hover:brightness-110 transition-all disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          {isSubmitting && (
            <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
          )}
          {isSubmitting ? 'Submitting…' : resubmit ? 'Resubmit Order' : 'Submit Claim'}
        </button>
      )}
    </form>
  )
}

function Field({
  as,
  label,
  placeholder,
  value,
  onChange,
  options,
  error,
  score,
  readOnly,
}: {
  as?: 'input' | 'select'
  label: string
  placeholder: string
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void
  options?: { value: string; label: string }[]
  error?: string
  score?: number
  readOnly?: boolean
}) {
  const inputClass = "w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary dark:text-ink-dark-primary px-3 py-2 outline-none focus:border-neon-blue/50 transition-colors placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"

  return (
    <div>
      <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
        {label} {!readOnly && <span className="text-neon-red">*</span>}
        {error && <span className="text-neon-red font-normal ml-2">{error}</span>}
        {!error && <ScoreBadge score={score} />}
      </label>
      {as === 'select' ? (
        <select
          value={value}
          onChange={onChange}
          disabled={readOnly}
          className={inputClass}
        >
          {options?.map(opt => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      ) : (
        <input
          type="text"
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          disabled={readOnly}
          className={inputClass}
        />
      )}
    </div>
  )
}
