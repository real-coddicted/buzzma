import { useEffect, useRef } from 'react'
import { Button } from '../../../components/ui/Button'

interface IOSInstallInstructionsModalProps {
  open: boolean
  onClose: () => void
}

export function IOSInstallInstructionsModal({ open, onClose }: IOSInstallInstructionsModalProps) {
  const closeButtonRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    function onKey(e: globalThis.KeyboardEvent) {
      if (e.key === 'Escape') {
        onClose()
      }
    }
    if (open) {
      document.body.style.overflow = 'hidden'
      document.addEventListener('keydown', onKey)
      closeButtonRef.current?.focus()
    }
    return () => {
      document.body.style.overflow = ''
      document.removeEventListener('keydown', onKey)
    }
  }, [open, onClose])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-[200] flex items-center justify-center p-4"
      aria-modal="true"
      role="dialog"
      aria-labelledby="ios-install-title"
      aria-describedby="ios-install-desc"
    >
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm" 
        onClick={onClose} 
        aria-hidden="true"
      />
      
      {/* Modal Content */}
      <div className="relative w-full max-w-md bg-surface-light-raised dark:bg-surface-dark-raised border border-surface-light-border dark:border-surface-dark-border rounded-2xl shadow-2xl p-6 md:p-8 flex flex-col gap-6 animate-fade-in text-ink-light-primary dark:text-ink-dark-primary">
        
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <h2 id="ios-install-title" className="text-lg font-bold text-ink-light-primary dark:text-ink-dark-primary">
              Install Buzzma
            </h2>
            <p id="ios-install-desc" className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary mt-1">
              Add Buzzma to your Home Screen to access it quickly as a full-screen app.
            </p>
          </div>
          <button
            ref={closeButtonRef}
            onClick={onClose}
            className="p-1.5 rounded-lg text-ink-light-muted dark:text-ink-dark-muted hover:text-ink-light-primary dark:hover:text-ink-dark-primary hover:bg-surface-light-hover dark:hover:bg-surface-dark-hover transition-colors"
            aria-label="Close modal"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Steps */}
        <div className="flex flex-col gap-4">
          {/* Step 1 */}
          <div className="flex items-center gap-3 p-3 bg-surface-light-hover/40 dark:bg-surface-dark-hover/20 rounded-xl border border-surface-light-border/50 dark:border-surface-dark-border/20">
            <span className="flex items-center justify-center w-6 h-6 rounded-full bg-neon-blue/10 text-neon-blue text-xs font-bold flex-shrink-0">
              1
            </span>
            <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary flex-grow">
              Tap the <span className="font-semibold text-neon-blue">Share</span> button in Safari.
            </span>
            <div className="w-8 h-8 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover flex items-center justify-center text-ink-light-primary dark:text-ink-dark-primary flex-shrink-0 border border-surface-light-border/50 dark:border-surface-dark-border/30">
              <svg className="w-4 h-4 text-neon-blue" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8" />
                <polyline points="16 6 12 2 8 6" />
                <line x1="12" y1="2" x2="12" y2="15" />
              </svg>
            </div>
          </div>

          {/* Step 2 */}
          <div className="flex items-center gap-3 p-3 bg-surface-light-hover/40 dark:bg-surface-dark-hover/20 rounded-xl border border-surface-light-border/50 dark:border-surface-dark-border/20">
            <span className="flex items-center justify-center w-6 h-6 rounded-full bg-neon-purple/10 text-neon-purple text-xs font-bold flex-shrink-0">
              2
            </span>
            <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary flex-grow">
              Scroll down and tap <span className="font-semibold text-neon-purple">"Add to Home Screen"</span>.
            </span>
            <div className="w-8 h-8 rounded-lg bg-surface-light-hover dark:bg-surface-dark-hover flex items-center justify-center text-ink-light-primary dark:text-ink-dark-primary flex-shrink-0 border border-surface-light-border/50 dark:border-surface-dark-border/30">
              <svg className="w-4 h-4 text-neon-purple" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                <line x1="12" y1="8" x2="12" y2="16" />
                <line x1="8" y1="12" x2="16" y2="12" />
              </svg>
            </div>
          </div>

          {/* Step 3 */}
          <div className="flex items-center gap-3 p-3 bg-surface-light-hover/40 dark:bg-surface-dark-hover/20 rounded-xl border border-surface-light-border/50 dark:border-surface-dark-border/20">
            <span className="flex items-center justify-center w-6 h-6 rounded-full bg-neon-green/10 text-neon-green text-xs font-bold flex-shrink-0">
              3
            </span>
            <span className="text-sm font-medium text-ink-light-primary dark:text-ink-dark-primary flex-grow">
              Tap <span className="font-semibold text-neon-green">"Add"</span> in the top right corner.
            </span>
            <div className="px-2 py-1 rounded bg-surface-light-hover dark:bg-surface-dark-hover flex items-center justify-center text-[10px] font-black text-neon-green border border-surface-light-border/50 dark:border-surface-dark-border/30 uppercase select-none">
              Add
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex justify-end gap-2 mt-2">
          <Button variant="primary" size="md" onClick={onClose} className="w-full" aria-label="Got it, close modal">
            Got it
          </Button>
        </div>
      </div>
    </div>
  )
}
