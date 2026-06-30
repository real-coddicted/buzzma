import { Button } from '../../../components/ui/Button'

interface OfflinePageProps {
  onDismiss: () => void
}

export function OfflinePage({ onDismiss }: OfflinePageProps) {
  const handleRetry = () => {
    window.location.reload()
  }

  return (
    <div className="fixed inset-0 z-[200] flex flex-col items-center justify-center bg-surface-light-base dark:bg-surface-dark-base p-6 text-center animate-fade-in" role="alertdialog" aria-labelledby="offline-title" aria-describedby="offline-desc">
      <div className="max-w-md w-full bg-surface-light-raised dark:bg-surface-dark-raised border border-surface-light-border dark:border-surface-dark-border rounded-2xl shadow-xl p-8 flex flex-col items-center gap-6">
        
        {/* Animated Offline Icon */}
        <div className="relative flex items-center justify-center w-20 h-20 rounded-full bg-neon-red/10 text-neon-red" aria-hidden="true">
          <span className="absolute inline-flex h-full w-full rounded-full bg-neon-red/15 animate-ping opacity-75" />
          <svg
            className="w-10 h-10 relative z-10"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M18.364 5.636a9 9 0 010 12.728m0 0l-2.829-2.829m2.829 2.829L21 21M15.536 8.464a5 5 0 010 7.072m0 0l-2.829-2.829m-4.243 2.829a4.978 4.978 0 01-1.414-3.536 5 5 0 011.414-3.536m0 0L5.636 5.636M8.464 15.536L3 21"
            />
          </svg>
        </div>

        {/* Text Details */}
        <div>
          <h2 id="offline-title" className="text-xl font-bold text-ink-light-primary dark:text-ink-dark-primary">
            Connection Lost
          </h2>
          <p id="offline-desc" className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary mt-2 leading-relaxed">
            You are currently offline. Don't worry, the app will automatically reconnect as soon as your internet connection is restored.
          </p>
        </div>

        {/* Spinner Loader showing reconnection attempt */}
        <div className="flex items-center gap-2 text-xs text-neon-blue font-medium animate-pulse-slow" aria-live="polite">
          <span className="w-2 h-2 rounded-full bg-neon-blue animate-ping" aria-hidden="true" />
          Waiting for network connection...
        </div>

        {/* Manual Action */}
        <div className="flex flex-col gap-2 w-full mt-2">
          <Button variant="primary" size="md" onClick={handleRetry} className="w-full" aria-label="Retry loading connections and pages">
            Retry Connection
          </Button>
          <Button variant="ghost" size="md" onClick={onDismiss} className="w-full" aria-label="Dismiss and browse cached content offline">
            Browse Offline
          </Button>
        </div>
      </div>
    </div>
  )
}
