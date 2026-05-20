import type { components } from '../types/api'

export type FeedbackCategory = 'ui' | 'performance' | 'features' | 'bugs' | 'other'
export type ApiFeedbackCategory = components['schemas']['FeedbackRequestDto']['category']

export const CATEGORY_MAP: Record<FeedbackCategory, ApiFeedbackCategory> = {
  ui: 'FEEDBACK_CATEGORY_UI_DESIGN',
  performance: 'FEEDBACK_CATEGORY_PERFORMANCE',
  features: 'FEEDBACK_CATEGORY_FEATURES',
  bugs: 'FEEDBACK_CATEGORY_BUG_REPORT',
  other: 'FEEDBACK_CATEGORY_OTHER',
}

export const CATEGORIES: { value: FeedbackCategory; label: string }[] = [
  { value: 'ui', label: 'UI / Design' },
  { value: 'performance', label: 'Performance' },
  { value: 'features', label: 'Features' },
  { value: 'bugs', label: 'Bug Report' },
  { value: 'other', label: 'Other' },
]


export const STAR_LABELS = ['', 'Poor', 'Fair', 'Good', 'Great', 'Excellent']

