import { useState, useEffect } from 'react'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { IconCheck, IconTicket } from '../components/ui/icons'
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

interface RaiseTicketFormProps {
  onSubmitted?: () => void
  onCancel?: () => void
}

export function RaiseTicketForm({ onSubmitted, onCancel }: RaiseTicketFormProps) {
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
    if (!form.categoryId) e.categoryId = 'Required'
    if (subCategories.length > 0 && !form.subCategoryId) e.subCategoryId = 'Required'
    if (showOrderId && !form.orderId.trim()) e.orderId = 'Required'
    if (!form.description.trim()) e.description = 'Required'
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
      onSubmitted?.()
    }, 800)
  }

  function reset() {
    setForm(EMPTY)
    setErrors({})
    setSubmitted(false)
  }

  if (submitted) {
    return (
      <div className="text-center space-y-4 py-6">
        <div className="w-14 h-14 rounded-full bg-neon-green/10 border border-neon-green/30 flex items-center justify-center mx-auto">
          <IconCheck size={24} className="text-neon-green" />
        </div>
        <h2 className="text-base font-bold text-ink-light-primary dark:text-ink-dark-primary">Ticket submitted</h2>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
          {"We've received your ticket and will get back to you shortly."}
        </p>
        <div className="flex justify-center gap-2">
          <Button variant="secondary" size="sm" onClick={reset}>Submit another</Button>
          {onCancel && <Button variant="primary" size="sm" onClick={onCancel}>Close</Button>}
        </div>
      </div>
    )
  }

  return (
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

      <div className="flex justify-end gap-2 pt-1">
        {onCancel && (
          <Button type="button" variant="secondary" size="sm" onClick={onCancel}>
            Cancel
          </Button>
        )}
        <Button type="submit" variant="primary" size="sm" loading={loading}>
          Submit Ticket
        </Button>
      </div>
    </form>
  )
}

// ── Modal ────────────────────────────────────────────────────────────────────

interface RaiseTicketModalProps {
  open: boolean
  onClose: () => void
}

export function RaiseTicketModal({ open, onClose }: RaiseTicketModalProps) {
  useEffect(() => {
    function onKey(e: globalThis.KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    if (open) document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [open, onClose])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      aria-modal="true"
      role="dialog"
      aria-labelledby="raise-ticket-modal-title"
    >
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative w-full max-w-xl max-h-[90vh] flex flex-col rounded-2xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-surface-light-border dark:border-surface-dark-border">
          <div>
            <h2 id="raise-ticket-modal-title" className="text-sm font-bold text-ink-light-primary dark:text-ink-dark-primary">
              Raise a Ticket
            </h2>
            <p className="text-xs text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
              Describe your issue and we'll look into it.
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
            aria-label="Close"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="overflow-y-auto flex-1 px-6 py-5">
          <RaiseTicketForm onCancel={onClose} />
        </div>
      </div>
    </div>
  )
}

// ── Button ───────────────────────────────────────────────────────────────────

interface RaiseTicketButtonProps {
  size?: 'sm' | 'md' | 'lg'
}

export function RaiseTicketButton({ size = 'md' }: RaiseTicketButtonProps) {
  const [open, setOpen] = useState(false)
  return (
    <>
      <Button variant="primary" size={size} leftIcon={<IconTicket size={14} />} onClick={() => setOpen(true)}>
        Raise a Ticket
      </Button>
      <RaiseTicketModal open={open} onClose={() => setOpen(false)} />
    </>
  )
}

// ── Page ─────────────────────────────────────────────────────────────────────

export function RaiseTicket() {
  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">Raise a Ticket</h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          Describe your issue and we'll look into it.
        </p>
      </div>
      <Card>
        <RaiseTicketForm />
      </Card>
    </div>
  )
}
