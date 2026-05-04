import { useState, useEffect } from 'react'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { IconCheck } from '../components/ui/icons'
import { fetchTicketCategories } from '../api/ticketApi'
import type { TicketCategory } from '../types/TicketTypes'

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
  categoryId: '',
  subCategoryId: '',
  orderId: '',
  description: '',
}

export function RaiseTicket() {
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState<Partial<Record<string, string>>>({})
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState<TicketCategory[]>([])
  const [categoriesLoading, setCategoriesLoading] = useState(true)
  const [categoriesError, setCategoriesError] = useState(false)

  useEffect(() => {
    fetchTicketCategories()
      .then(setCategories)
      .catch(() => setCategoriesError(true))
      .finally(() => setCategoriesLoading(false))
  }, [])

  const selectedCategory = categories.find(c => c.id === form.categoryId)
  const subCategories = selectedCategory?.subCategories ?? []
  const selectedSubCategory = subCategories.find(s => s.id === form.subCategoryId)
  const showOrderId = selectedSubCategory?.requiresOrderId ?? false

  function set(field: keyof typeof EMPTY, value: string) {
    setForm(prev => ({
      ...prev,
      [field]: value,
      ...(field === 'categoryId' ? { subCategoryId: '', orderId: '' } : {}),
    }))
    setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  function validate() {
    const e: Partial<Record<string, string>> = {}
    if (!form.categoryId)           e.categoryId = 'Required'
    if (subCategories.length > 0 && !form.subCategoryId) e.subCategoryId = 'Required'
    if (showOrderId && !form.orderId.trim()) e.orderId = 'Required'
    if (!form.description.trim())   e.description = 'Required'

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

          {/* Category + Sub-category */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={labelClass}>Category *</label>
              <select
                className={inputClass}
                value={form.categoryId}
                onChange={e => set('categoryId', e.target.value)}
                disabled={categoriesLoading}
              >
                <option value="">
                  {categoriesLoading ? 'Loading…' : categoriesError ? 'Failed to load' : '— Select —'}
                </option>
                {categories.map(c => (
                  <option key={c.id} value={c.id}>{c.displayName}</option>
                ))}
              </select>
              {errors.categoryId && <p className={errorClass}>{errors.categoryId}</p>}
            </div>

            <div>
              <label className={labelClass}>
                Sub-category{subCategories.length > 0 ? ' *' : ''}
              </label>
              <select
                className={inputClass}
                value={form.subCategoryId}
                onChange={e => set('subCategoryId', e.target.value)}
                disabled={!form.categoryId || subCategories.length === 0}
              >
                <option value="">
                  {!form.categoryId ? '— Select category first —' : subCategories.length === 0 ? '— None —' : '— Select —'}
                </option>
                {subCategories.map(s => (
                  <option key={s.id} value={s.id}>{s.displayName}</option>
                ))}
              </select>
              {errors.subCategoryId && <p className={errorClass}>{errors.subCategoryId}</p>}
            </div>
          </div>

          {/* Order ID */}
          {showOrderId && (
            <div>
              <label className={labelClass}>Order ID *</label>
              <input
                className={inputClass}
                type="text"
                placeholder="e.g. ORD-123456"
                value={form.orderId}
                onChange={e => set('orderId', e.target.value)}
              />
              {errors.orderId && <p className={errorClass}>{errors.orderId}</p>}
            </div>
          )}

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
