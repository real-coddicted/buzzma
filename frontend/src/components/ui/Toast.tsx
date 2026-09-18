import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import type { RefObject } from 'react'
import { addToast, removeToast, setToastHeight, getToastOffset, subscribeToasts } from './toastStack'

type ToastType = 'success' | 'error'

interface ToastProps {
  message: string
  type?: ToastType
  duration?: number
  onDismiss: () => void
  actionLabel?: string
  onAction?: () => void
}

const typeClasses: Record<ToastType, string> = {
  success: 'border-neon-green/40 bg-neon-green/10 text-neon-green',
  error:   'border-neon-red/40 bg-neon-red/10 text-neon-red',
}

const iconPath: Record<ToastType, string> = {
  success: 'M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
  error:   'M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z',
}

/** Distance (px) from the bottom of the viewport to the lowest toast. */
const TOAST_BASE_OFFSET_PX = 20

/**
 * Places this Toast in the shared stack so concurrent toasts don't overlap, and
 * returns how far above the bottom anchor it should sit. Heights are measured
 * from the DOM rather than assumed, so multi-line messages still stack cleanly.
 */
function useToastStackOffset(ref: RefObject<HTMLDivElement>): number {
  const idRef = useRef<string | null>(null)
  const [, forceUpdate] = useState(0)

  useLayoutEffect(() => {
    idRef.current = addToast()
    forceUpdate(n => n + 1)
    return () => {
      if (idRef.current) removeToast(idRef.current)
      idRef.current = null
    }
  }, [])

  // Re-measure after every render: the message (and so the height) can change.
  // setToastHeight is a no-op when the height is unchanged, so this settles.
  useLayoutEffect(() => {
    if (!idRef.current || !ref.current) return
    setToastHeight(idRef.current, ref.current.getBoundingClientRect().height)
  })

  useEffect(() => subscribeToasts(() => forceUpdate(n => n + 1)), [])

  return idRef.current ? getToastOffset(idRef.current) : 0
}

export function Toast({ message, type = 'success', duration = 10000, onDismiss, actionLabel, onAction }: ToastProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const stackOffset = useToastStackOffset(containerRef)

  useEffect(() => {
    const timer = setTimeout(onDismiss, duration)
    return () => clearTimeout(timer)
  }, [duration, onDismiss])

  return (
    <div
      ref={containerRef}
      className="fixed right-5 z-50 animate-fade-in"
      style={{ bottom: `${TOAST_BASE_OFFSET_PX + stackOffset}px` }}
    >
      <div className={[
        'flex items-start gap-3 px-4 py-3 rounded-xl border shadow-card-dark max-w-sm',
        typeClasses[type],
      ].join(' ')}>
        <svg className="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.75}>
          <path strokeLinecap="round" strokeLinejoin="round" d={iconPath[type]} />
        </svg>
        <p className="text-sm font-medium leading-snug">{message}</p>
        {actionLabel && onAction && (
          <button
            onClick={onAction}
            className="flex-shrink-0 text-xs font-semibold underline hover:no-underline"
          >
            {actionLabel}
          </button>
        )}
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
