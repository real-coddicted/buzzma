import { useEffect } from 'react'

type ToastType = 'success' | 'error'

interface ToastProps {
  message: string
  type?: ToastType
  duration?: number
  onDismiss: () => void
}

const typeClasses: Record<ToastType, string> = {
  success: 'border-neon-green/40 bg-neon-green/10 text-neon-green',
  error:   'border-neon-red/40 bg-neon-red/10 text-neon-red',
}

const iconPath: Record<ToastType, string> = {
  success: 'M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
  error:   'M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z',
}

export function Toast({ message, type = 'success', duration = 3000, onDismiss }: ToastProps) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, duration)
    return () => clearTimeout(timer)
  }, [duration, onDismiss])

  return (
    <div className="fixed top-5 right-5 z-50 animate-fade-in">
      <div className={[
        'flex items-start gap-3 px-4 py-3 rounded-xl border shadow-card-dark max-w-sm',
        typeClasses[type],
      ].join(' ')}>
        <svg className="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
          <path strokeLinecap="round" strokeLinejoin="round" d={iconPath[type]} />
        </svg>
        <p className="text-sm font-medium leading-snug">{message}</p>
        <button
          onClick={onDismiss}
          className="ml-auto flex-shrink-0 opacity-60 hover:opacity-100 transition-opacity"
          aria-label="Dismiss"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
  )
}
