import { useState } from 'react'
import { submitFeedback } from '../../../api/feedbackApi'
import { CATEGORY_MAP, type FeedbackCategory } from '../../../constants/feedback'
import { Button } from '../Button'
import { Card } from '../Card'
import { FeedbackCategorySelector } from './FeedbackCategorySelector'
import { FeedbackStarRating } from './FeedbackStarRating'
import { FeedbackSuccessState } from './FeedbackSuccessState'
import { FeedbackSubmitterCard } from './FeedbackSubmitterCard'

interface FeedbackFormProps {
  submitterName: string
  submitterEmail: string
}

const labelClass =
  'block text-[11px] font-semibold uppercase tracking-wider text-ink-light-muted dark:text-ink-dark-muted mb-1.5'

const inputClass = [
  'w-full rounded-lg border bg-surface-light-hover dark:bg-surface-dark-hover',
  'border-surface-light-border dark:border-surface-dark-border',
  'text-xs text-ink-light-primary dark:text-ink-dark-primary',
  'placeholder:text-ink-light-muted dark:placeholder:text-ink-dark-muted',
  'px-3 py-2 outline-none focus:border-neon-blue/60 focus:ring-1 focus:ring-neon-blue/30 transition-all',
].join(' ')

export function FeedbackForm({ submitterName, submitterEmail }: FeedbackFormProps) {
  const [rating, setRating] = useState(0)
  const [category, setCategory] = useState<FeedbackCategory | ''>('')
  const [message, setMessage] = useState('')
  const [errors, setErrors] = useState<{ rating?: string; category?: string; message?: string }>({})
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)

  function validate() {
    const e: typeof errors = {}
    if (rating === 0) e.rating = 'Please select a rating'
    if (!category) e.category = 'Please select a category'
    if (!message.trim()) e.message = 'Please enter your feedback'
    else if (message.trim().length < 10) e.message = 'Please provide at least 10 characters'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validate()) return
    if (!category) return

    setLoading(true)
    setApiError(null)
    try {
      await submitFeedback({
        rating,
        category: CATEGORY_MAP[category],
        feedback: message.trim(),
      })
      setSubmitted(true)
    } catch (err) {
      const msg = err instanceof Error && err.message.includes('Session expired')
        ? err.message
        : 'Something went wrong. Please try again.'
      setApiError(msg)
    } finally {
      setLoading(false)
    }
  }

  function handleReset() {
    setRating(0)
    setCategory('')
    setMessage('')
    setErrors({})
    setSubmitted(false)
    setApiError(null)
  }

  if (submitted) {
    return <FeedbackSuccessState onReset={handleReset} />
  }

  return (
    <Card>
      <form onSubmit={handleSubmit} noValidate className="space-y-6">
        <div>
          <label className={labelClass}>Overall Rating *</label>
          <FeedbackStarRating value={rating} onChange={v => { setRating(v); setErrors(prev => ({ ...prev, rating: undefined })) }} />
          {errors.rating && <p className="text-[10px] text-neon-red mt-1">{errors.rating}</p>}
        </div>

        <div>
          <label className={labelClass}>Category *</label>
          <FeedbackCategorySelector
            value={category}
            onChange={value => {
              setCategory(value)
              setErrors(prev => ({ ...prev, category: undefined }))
            }}
          />
          {errors.category && <p className="text-[10px] text-neon-red mt-1">{errors.category}</p>}
        </div>

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

        <FeedbackSubmitterCard name={submitterName} email={submitterEmail} />

        {apiError && (
          <p className="text-xs text-neon-red text-center">{apiError}</p>
        )}

        <div className="flex justify-end">
          <Button type="submit" variant="primary" size="md" loading={loading}>
            Submit Feedback
          </Button>
        </div>
      </form>
    </Card>
  )
}

