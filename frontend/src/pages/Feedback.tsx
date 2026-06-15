import { useState } from 'react'
import { APP_NAME } from '../constants/app'
import { FeedbackForm } from '../components/ui/feedback/FeedbackForm'
import { getCurrentUser } from '../api/client'

export function Feedback() {
  const [currentUser] = useState(() => getCurrentUser())

  const submitterName = currentUser?.name?.trim() || 'Signed-in user'
  const submitterEmail = currentUser?.email?.trim() || 'Email not available'

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
          Portal Feedback
        </h1>
        <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted mt-0.5">
          Help us improve {APP_NAME} — your feedback goes directly to the product team.
        </p>
      </div>

      <FeedbackForm submitterName={submitterName} submitterEmail={submitterEmail} />
    </div>
  )
}
