import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { remainingDelay, runAfterMinDuration } from './minDuration'

describe('remainingDelay', () => {
  it('returns the remaining time when less than minMs has elapsed', () => {
    expect(remainingDelay(1000, 300, 1100)).toBe(200)
  })

  it('returns 0 when exactly minMs has elapsed', () => {
    expect(remainingDelay(1000, 300, 1300)).toBe(0)
  })

  it('returns 0 when more than minMs has elapsed', () => {
    expect(remainingDelay(1000, 300, 2000)).toBe(0)
  })

  it('defaults to Date.now() when now is not provided', () => {
    const start = Date.now() - 50
    const remaining = remainingDelay(start, 300)
    expect(remaining).toBeGreaterThan(0)
    expect(remaining).toBeLessThanOrEqual(300)
  })
})

describe('runAfterMinDuration', () => {
  beforeEach(() => vi.useFakeTimers())
  afterEach(() => vi.useRealTimers())

  it('calls onSettled immediately if minMs has already elapsed', async () => {
    const startTimeRef = { current: Date.now() - 500 }
    const onSettled = vi.fn()
    await Promise.resolve().then(() => runAfterMinDuration(Promise.resolve(), startTimeRef, 300, onSettled))
    await vi.advanceTimersByTimeAsync(0)
    expect(onSettled).toHaveBeenCalledTimes(1)
  })

  it('delays onSettled until minMs has elapsed', async () => {
    const startTimeRef = { current: Date.now() }
    const onSettled = vi.fn()
    runAfterMinDuration(Promise.resolve(), startTimeRef, 300, onSettled)
    await vi.advanceTimersByTimeAsync(0)
    expect(onSettled).not.toHaveBeenCalled()
    await vi.advanceTimersByTimeAsync(300)
    expect(onSettled).toHaveBeenCalledTimes(1)
  })

  it('does not call onSettled after the cleanup function runs', async () => {
    const startTimeRef = { current: Date.now() }
    const onSettled = vi.fn()
    const cleanup = runAfterMinDuration(Promise.resolve(), startTimeRef, 300, onSettled)
    await vi.advanceTimersByTimeAsync(0)
    cleanup()
    await vi.advanceTimersByTimeAsync(300)
    expect(onSettled).not.toHaveBeenCalled()
  })

  it('does not call onSettled when cancelled before a promise that settles past minMs', async () => {
    // minMs has already elapsed, so settling takes the immediate (no-timer) path —
    // which must still respect cancellation.
    const startTimeRef = { current: Date.now() - 500 }
    const onSettled = vi.fn()
    let resolvePromise: () => void = () => {}
    const promise = new Promise<void>(resolve => { resolvePromise = resolve })

    const cleanup = runAfterMinDuration(promise, startTimeRef, 300, onSettled)
    cleanup()
    resolvePromise()
    await vi.advanceTimersByTimeAsync(0)

    expect(onSettled).not.toHaveBeenCalled()
  })
})
