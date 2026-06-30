import { Button } from '../../../components/ui/Button'
import { usePwaUpdate } from '../hooks/usePwaUpdate'

export function UpdateNotification() {
  const { updateAvailable, updateApp, dismissUpdate } = usePwaUpdate()

  if (!updateAvailable) return null

  return (
    <div className="fixed bottom-4 left-4 right-4 md:right-auto md:left-4 md:max-w-md z-[110] animate-slide-in" role="alert" aria-labelledby="update-banner-title" aria-describedby="update-banner-desc">
      <div className="bg-surface-light-raised dark:bg-surface-dark-raised border border-neon-blue/30 dark:border-neon-blue/40 rounded-xl shadow-2xl p-4 flex items-center gap-4 backdrop-blur-md bg-opacity-95 dark:bg-opacity-95">
        
        {/* Info Icon Indicator */}
        <div className="w-10 h-10 rounded-full flex-shrink-0 bg-neon-blue/10 text-neon-blue flex items-center justify-center" aria-hidden="true">
          <svg className="w-6 h-6 animate-pulse-slow" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 1121.21 16h-2.197" />
          </svg>
        </div>

        {/* Content */}
        <div className="flex-grow">
          <h4 id="update-banner-title" className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            Update Available
          </h4>
          <p id="update-banner-desc" className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary mt-0.5">
            A new version of Buzzma is available.
          </p>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2 flex-shrink-0">
          <Button variant="primary" size="sm" onClick={updateApp} aria-label="Update application now">
            Update
          </Button>
          <Button variant="ghost" size="sm" onClick={dismissUpdate} aria-label="Dismiss update notification">
            Later
          </Button>
        </div>
      </div>
    </div>
  )
}
