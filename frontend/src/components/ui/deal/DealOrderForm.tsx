import { useState } from 'react'
import { IconCalendar } from '../icons'
import { ScreenshotUpload } from './ScreenshotUpload'
import type { ExtractionResponse } from '../../../api/extractionApi'

interface DealOrderFormFields {
  platform: string
  orderId: string
  amount: string
  productName: string
  sellerName: string
  orderDate: string
  accountName: string
}

interface DealOrderFormProps {
  onSubmit: (fields: DealOrderFormFields) => void
}

export function DealOrderForm({ onSubmit }: DealOrderFormProps) {
  const [fields, setFields] = useState<DealOrderFormFields>({
    platform:    '',
    orderId:     '',
    amount:      '',
    productName: '',
    sellerName:  '',
    orderDate:   '',
    accountName: '',
  })

  function set(key: keyof DealOrderFormFields) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setFields(prev => ({ ...prev, [key]: e.target.value }))
  }

  function handleExtraction(data: ExtractionResponse) {
    setFields(prev => ({
      ...prev,
      platform: data.platform || prev.platform,
      orderId: data.orderId || prev.orderId,
      amount: String(data.amount || prev.amount),
      productName: data.productName || prev.productName,
      sellerName: data.sellerName || prev.sellerName,
      orderDate: data.orderDate || prev.orderDate,
      accountName: data.orderedBy || prev.accountName,
    }))
  }

  const isValid = Object.values(fields).every(v => v.trim() !== '')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (isValid) onSubmit(fields)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <ScreenshotUpload
        label="Order Confirmation Screenshot"
        hint="Ensure the order ID, amount, and product name are clearly visible."
        onExtract={handleExtraction}
      />
      <Field 
        as="select"
        label="Platform" 
        placeholder="Select platform" 
        value={fields.platform} 
        onChange={set('platform')}
        options={[
          { value: '', label: 'Select platform' },
          { value: 'PLATFORM_AMAZON', label: 'Amazon' },
          { value: 'PLATFORM_FLIPKART', label: 'Flipkart' },
          { value: 'PLATFORM_NYKAA', label: 'Nykaa' },
          { value: 'PLATFORM_MYNTRA', label: 'Myntra' },
        ]}
      />
      <Field label="Order ID"         placeholder="e.g. 403-1234567-8901234" value={fields.orderId}     onChange={set('orderId')}     />
      <Field label="Amount"           placeholder="e.g. 1499"                value={fields.amount}      onChange={set('amount')}      />
      <Field label="Product Name"     placeholder="Enter product name"        value={fields.productName} onChange={set('productName')} />
      <Field label="Seller Name"      placeholder="Enter seller name"         value={fields.sellerName}  onChange={set('sellerName')}  />

      <div>
        <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
          Order Date <span className="text-neon-red">*</span>
        </label>
        <div className="relative">
          <input
            type="date"
            value={fields.orderDate}
            onChange={set('orderDate')}
            className="w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary dark:text-ink-dark-primary pl-9 pr-3 py-2 outline-none focus:border-neon-blue/50 transition-colors"
          />
          <IconCalendar size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink-light-muted dark:text-ink-dark-muted pointer-events-none" />
        </div>
      </div>

      <Field label="Account Name"     placeholder="Name on your account"      value={fields.accountName} onChange={set('accountName')} />

      <button
        type="submit"
        disabled={!isValid}
        className="w-full py-2.5 rounded-lg bg-neon-blue text-surface-dark-base text-sm font-semibold hover:brightness-110 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
      >
        Submit Claim
      </button>
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
}: {
  as?: 'input' | 'select'
  label: string
  placeholder: string
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void
  options?: { value: string; label: string }[]
}) {
  const inputClass = "w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary dark:text-ink-dark-primary px-3 py-2 outline-none focus:border-neon-blue/50 transition-colors placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"

  return (
    <div>
      <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
        {label} <span className="text-neon-red">*</span>
      </label>
      {as === 'select' ? (
        <select
          value={value}
          onChange={onChange}
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
          className={inputClass}
        />
      )}
    </div>
  )
}
