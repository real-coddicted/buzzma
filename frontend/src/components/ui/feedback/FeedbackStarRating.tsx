import { useState } from 'react'
import { STAR_LABELS } from '../../../constants/feedback'

interface FeedbackStarRatingProps {
  value: number
  onChange: (value: number) => void
}

export function FeedbackStarRating({ value, onChange }: FeedbackStarRatingProps) {
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

