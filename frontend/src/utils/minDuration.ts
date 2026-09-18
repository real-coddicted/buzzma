/** Delay (ms) remaining to reach `minMs` since `startTime`, or 0 if already elapsed. */
export function remainingDelay(startTime: number, minMs: number, now: number = Date.now()): number {
  return Math.max(0, minMs - (now - startTime))
}

/**
 * Runs `onSettled` once `promise` settles, delayed if needed so at least `minMs`
 * has elapsed since `startTimeRef.current`. Returns a cleanup function that
 * cancels the pending `onSettled` call (e.g. for a `useEffect` cleanup).
 */
export function runAfterMinDuration(
  promise: Promise<unknown>,
  startTimeRef: { current: number },
  minMs: number,
  onSettled: () => void,
): () => void {
  let cancelled = false
  let timeoutId: ReturnType<typeof setTimeout>
  promise.finally(() => {
    if (cancelled) return
    const remaining = remainingDelay(startTimeRef.current, minMs)
    if (remaining > 0) {
      timeoutId = setTimeout(() => { if (!cancelled) onSettled() }, remaining)
    } else {
      onSettled()
    }
  })
  return () => { cancelled = true; clearTimeout(timeoutId) }
}
