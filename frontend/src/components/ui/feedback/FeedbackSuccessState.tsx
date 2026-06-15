import { Button } from '../Button'
import { APP_NAME } from '../../../constants/app'
import { IconCheck } from '../icons'

interface FeedbackSuccessStateProps {
  onReset: () => void
}

export function FeedbackSuccessState({ onReset }: FeedbackSuccessStateProps) {
  return (
    <div className="max-w-lg mx-auto mt-16 text-center space-y-4">
      <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-neon-green/10 border border-neon-green/30 text-neon-green">
        <IconCheck size={24} />
      </div>
      <h2 className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary">
        Thanks for your feedback!
      </h2>
      <p className="text-sm text-ink-light-muted dark:text-ink-dark-muted">
        Your response has been recorded. We read every submission and use it to improve {APP_NAME}.
      </p>
      <Button variant="secondary" size="sm" onClick={onReset}>
        Submit another response
      </Button>
    </div>
  )
}

