import { useState } from 'react'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { IconCheck } from '../components/ui/icons'

type Category = 'ui' | 'performance' | 'features' | 'bugs' | 'other'

const CATEGORIES: { value: Category; label: string }[] = [
  { value: 'ui',          label: 'UI / Design' },
  { value: 'performance', label: 'Performance' },
  { value: 'features',    label: 'Features' },
  { value: 'bugs',        label: 'Bug Report' },
  { value: 'other',       label: 'Other' },
]

const STAR_LABELS = ['', 'Poor', 'Fair', 'Good', 'Great', 'Excellent']

const labelClass =
  'block text-[11px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-1.5'

const inputClass = [
  'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover',
  'border-surface-light-border dark:border-surface-dark-border',
  'text-xs text-ink-light-primary dark:text-ink-dark-primary',
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
  'px-3 py-2 outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all',
].join(' ')

function StarRating({ value, onChange }: { value: number; onChange: (v: number) => void }) {
  const [hovered, setHovered] = useState(0)
  const active = hovered || value

  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map(star => (
        <button
          key={star}
          type="button"
          onClick={() => onChange(star)}
          onMouseEnter={() => setHovered(star)}
          onMouseLeave={() => setHovered(0)}
          className="focus:outline-none transition-transform hover:scale-110"
          aria-label={`Rate ${star} out of 5`}
        >
          <svg
            width="28"
            height="28"
            viewBox="0 0 24 24"
            fill={star <= active ? 'currentColor' : 'none'}
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
            className={star <= active ? 'text-neon-yellow' : 'text-ink-light-muted dark:text-ink-dark-muted'}
          >
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
          </svg>
        </button>
      ))}
      {active > 0 && (
        <span className="ml-2 text-xs font-semibold text-neon-yellow">
          {STAR_LABELS[active]}
        </span>
      )}
    </div>
  )
}

export function Feedback() {
  const [rating, setRating] = useState(0)
  const [category, setCategory] = useState<Category | ''>('')
  const [message, setMessage] = useState('')
  const [errors, setErrors] = useState<{ rating?: string; category?: string; message?: string }>({})
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)

  function validate() {
    const e: typeof errors = {}
    if (rating === 0) e.rating = 'Please select a rating'
    if (!category) e.category = 'Please select a category'
    if (!message.trim()) e.message = 'Please enter your feedback'
    else if (message.trim().length < 10) e.message = 'Please provide at least 10 characters'
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

  function handleReset() {
    setRating(0)
    setCategory('')
    setMessage('')
    setErrors({})
    setSubmitted(false)
  }

  if (submitted) {
    return (
      <div className="max-w-lg mx-auto mt-16 text-center space-y-4">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-neon-green/10 border border-neon-green/30 text-neon-green">
          <IconCheck size={24} />
        </div>
        <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Thanks for your feedback!
        </h2>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
          Your response has been recorded. We read every submission and use it to improve Pulse.
        </p>
        <Button variant="secondary" size="sm" onClick={handleReset}>
          Submit another response
        </Button>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Portal Feedback
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          Help us improve Pulse — your feedback goes directly to the product team.
        </p>
      </div>

      <Card>
        <form onSubmit={handleSubmit} noValidate className="space-y-6">
          {/* Rating */}
          <div>
            <label className={labelClass}>Overall Rating *</label>
            <StarRating value={rating} onChange={v => { setRating(v); setErrors(prev => ({ ...prev, rating: undefined })) }} />
            {errors.rating && <p className="text-[10px] text-neon-red mt-1">{errors.rating}</p>}
          </div>

          {/* Category */}
          <div>
            <label className={labelClass}>Category *</label>
            <div className="flex flex-wrap gap-2">
              {CATEGORIES.map(c => (
                <button
                  key={c.value}
                  type="button"
                  onClick={() => { setCategory(c.value); setErrors(prev => ({ ...prev, category: undefined })) }}
                  className={[
                    'px-3 py-1.5 rounded-full text-xs font-medium border transition-all',
                    category === c.value
                      ? 'bg-neon-blue/10 text-neon-blue border-neon-blue/30'
                      : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover',
                  ].join(' ')}
                >
                  {c.label}
                </button>
              ))}
            </div>
            {errors.category && <p className="text-[10px] text-neon-red mt-1">{errors.category}</p>}
          </div>

          {/* Message */}
          <div>
            <label className={labelClass}>Your Feedback *</label>
            <textarea
              className={[inputClass, 'resize-none leading-relaxed'].join(' ')}
              rows={5}
              placeholder="Tell us what you think — what's working well, what could be better, or anything else on your mind."
              value={message}
              onChange={e => { setMessage(e.target.value); setErrors(prev => ({ ...prev, message: undefined })) }}
            />
            <div className="flex items-center justify-between mt-1">
              {errors.message
                ? <p className="text-[10px] text-neon-red">{errors.message}</p>
                : <span />
              }
              <span className={[
                'text-[10px] tabular-nums',
                message.length > 1000 ? 'text-neon-red' : 'text-ink-light-muted dark:text-ink-dark-muted',
              ].join(' ')}>
                {message.length} / 1000
              </span>
            </div>
          </div>

          {/* Submitter info (read-only, from session) */}
          <div className="flex items-center gap-3 p-3 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border">
            <div
              className="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold text-white flex-shrink-0"
              style={{ background: 'linear-gradient(135deg, #ff79c6 0%, #bd93f9 100%)' }}
            >
              A
            </div>
            <div className="min-w-0">
              <div className="text-xs font-semibold text-ink-light-primary dark:text-ink-dark-primary">
                Alex Rivera
              </div>
              <div className="text-[11px] text-ink-light-muted dark:text-ink-dark-muted">
                Submitting as alex@pulse.io
              </div>
            </div>
          </div>

          <div className="flex justify-end">
            <Button type="submit" variant="primary" size="md" loading={loading}>
              Submit Feedback
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
