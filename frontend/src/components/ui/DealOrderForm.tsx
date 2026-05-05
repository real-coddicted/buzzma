import { useState } from 'react'
import { IconCalendar } from './icons'

interface DealOrderFormFields {
  orderId: string
  amount: string
  productName: string
  soldBy: string
  orderDate: string
  accountName: string
}

interface DealOrderFormProps {
  onSubmit: (fields: DealOrderFormFields) => void
}

export function DealOrderForm({ onSubmit }: DealOrderFormProps) {
  const [fields, setFields] = useState<DealOrderFormFields>({
    orderId:     '',
    amount:      '',
    productName: '',
    soldBy:      '',
    orderDate:   '',
    accountName: '',
  })

  function set(key: keyof DealOrderFormFields) {
    return (e: React.ChangeEvent<HTMLInputElement>) =>
      setFields(prev => ({ ...prev, [key]: e.target.value }))
  }

  const isValid = Object.values(fields).every(v => v.trim() !== '')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (isValid) onSubmit(fields)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <Field label="Order ID"         placeholder="e.g. 403-1234567-8901234" value={fields.orderId}     onChange={set('orderId')}     />
      <Field label="Amount"           placeholder="e.g. ₹1,499"              value={fields.amount}      onChange={set('amount')}      />
      <Field label="Product Name"     placeholder="Enter product name"        value={fields.productName} onChange={set('productName')} />
      <Field label="Seller / Sold by" placeholder="Enter seller name"         value={fields.soldBy}      onChange={set('soldBy')}      />

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
  label,
  placeholder,
  value,
  onChange,
}: {
  label: string
  placeholder: string
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
}) {
  return (
    <div>
      <label className="block text-xs font-semibold text-ink-light-secondary dark:text-ink-dark-secondary mb-1.5">
        {label} <span className="text-neon-red">*</span>
      </label>
      <input
        type="text"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        className="w-full text-sm rounded-lg border border-surface-light-border dark:border-surface-dark-border bg-surface-light-hover dark:bg-surface-dark-hover text-ink-light-primary dark:text-ink-dark-primary px-3 py-2 outline-none focus:border-neon-blue/50 transition-colors placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted"
      />
    </div>
  )
}
