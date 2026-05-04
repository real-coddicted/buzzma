import { useState } from 'react'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { IconCheck } from '../components/ui/icons'

type TicketCategory = 'bug' | 'feature' | 'billing' | 'access' | 'other'

const CATEGORIES: { value: TicketCategory; label: string }[] = [
  { value: 'bug',     label: 'Bug / Error' },
  { value: 'feature', label: 'Feature Request' },
  { value: 'billing', label: 'Billing' },
  { value: 'access',  label: 'Access / Permissions' },
  { value: 'other',   label: 'Other' },
]

const labelClass =
  'block text-[11px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-1.5'

const inputClass = [
  'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover',
  'border-surface-light-border dark:border-surface-dark-border',
  'text-xs text-ink-light-primary dark:text-ink-dark-primary',
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
  'px-3 py-2 outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all',
].join(' ')

const errorClass = 'text-[10px] text-neon-red mt-1'

const EMPTY = {
  title: '',
  category: '' as TicketCategory | '',
  description: '',
  stepsToReproduce: '',
  email: '',
}

export function RaiseTicket() {
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState<Partial<Record<string, string>>>({})
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)

  function set(field: keyof typeof EMPTY, value: string) {
    setForm(prev => ({ ...prev, [field]: value }))
    setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  function validate() {
    const e: Partial<Record<string, string>> = {}
    if (!form.title.trim())       e.title = 'Required'
    if (!form.category)           e.category = 'Required'
    if (!form.description.trim()) e.description = 'Required'
    if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
      e.email = 'Enter a valid email'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    setTimeout(() => {
      setLoading(false)
      setSubmitted(true)
    }, 800)
  }

  function reset() {
    setForm(EMPTY)
    setErrors({})
    setSubmitted(false)
  }

  if (submitted) {
    return (
      <div className="max-w-lg mx-auto mt-20 text-center space-y-4">
        <div className="w-14 h-14 rounded-full bg-neon-green/10 border border-neon-green/30 flex items-center justify-center mx-auto">
          <IconCheck size={24} className="text-neon-green" />
        </div>
        <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary">Ticket submitted</h2>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
          We've received your ticket and will get back to you shortly.
        </p>
        <Button variant="secondary" size="sm" onClick={reset}>Submit another</Button>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">Raise a Ticket</h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          Describe your issue and we'll look into it.
        </p>
      </div>

      <Card>
        <form onSubmit={handleSubmit} noValidate className="space-y-5">
          {/* Title */}
          <div>
            <label className={labelClass}>Subject *</label>
            <input
              className={inputClass}
              type="text"
              placeholder="Brief summary of the issue"
              value={form.title}
              onChange={e => set('title', e.target.value)}
            />
            {errors.title && <p className={errorClass}>{errors.title}</p>}
          </div>

          {/* Category */}
          <div>
            <label className={labelClass}>Category *</label>
            <select
              className={inputClass}
              value={form.category}
              onChange={e => set('category', e.target.value)}
            >
              <option value="">— Select —</option>
              {CATEGORIES.map(c => (
                <option key={c.value} value={c.value}>{c.label}</option>
              ))}
            </select>
            {errors.category && <p className={errorClass}>{errors.category}</p>}
          </div>

          {/* Description */}
          <div>
            <label className={labelClass}>Description *</label>
            <textarea
              className={[inputClass, 'resize-none'].join(' ')}
              rows={4}
              placeholder="Describe the issue in detail"
              value={form.description}
              onChange={e => set('description', e.target.value)}
            />
            {errors.description && <p className={errorClass}>{errors.description}</p>}
          </div>

          {/* Steps to reproduce */}
          <div>
            <label className={labelClass}>Steps to reproduce <span className="normal-case font-normal">(optional)</span></label>
            <textarea
              className={[inputClass, 'resize-none'].join(' ')}
              rows={3}
              placeholder="1. Go to…&#10;2. Click on…&#10;3. See error"
              value={form.stepsToReproduce}
              onChange={e => set('stepsToReproduce', e.target.value)}
            />
          </div>

          {/* Contact email */}
          <div>
            <label className={labelClass}>Contact email <span className="normal-case font-normal">(optional)</span></label>
            <input
              className={inputClass}
              type="email"
              placeholder="you@example.com"
              value={form.email}
              onChange={e => set('email', e.target.value)}
            />
            {errors.email && <p className={errorClass}>{errors.email}</p>}
          </div>

          <div className="flex justify-end pt-1">
            <Button type="submit" variant="primary" size="sm" loading={loading}>
              Submit Ticket
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
