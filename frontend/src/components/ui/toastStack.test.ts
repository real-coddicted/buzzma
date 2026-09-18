import { describe, it, expect, beforeEach, vi } from 'vitest'
import {
  addToast,
  removeToast,
  setToastHeight,
  getToastIndex,
  getToastOffset,
  subscribeToasts,
  __resetToastStackForTests,
} from './toastStack'

/** Matches TOAST_GAP_PX in toastStack.ts. */
const GAP = 12

describe('toastStack', () => {
  beforeEach(() => __resetToastStackForTests())

  it('assigns increasing stack indices as toasts are added', () => {
    const id1 = addToast()
    const id2 = addToast()
    const id3 = addToast()
    expect(getToastIndex(id1)).toBe(0)
    expect(getToastIndex(id2)).toBe(1)
    expect(getToastIndex(id3)).toBe(2)
  })

  it('shifts remaining indices down when an earlier toast is removed', () => {
    const id1 = addToast()
    const id2 = addToast()
    const id3 = addToast()
    removeToast(id1)
    expect(getToastIndex(id2)).toBe(0)
    expect(getToastIndex(id3)).toBe(1)
  })

  it('returns -1 for an unknown or removed id', () => {
    const id1 = addToast()
    removeToast(id1)
    expect(getToastIndex(id1)).toBe(-1)
  })

  it('notifies subscribers when the stack changes', () => {
    const listener = vi.fn()
    const unsubscribe = subscribeToasts(listener)
    addToast()
    expect(listener).toHaveBeenCalledTimes(1)
    unsubscribe()
    addToast()
    expect(listener).toHaveBeenCalledTimes(1)
  })
})

describe('getToastOffset', () => {
  beforeEach(() => __resetToastStackForTests())

  it('places the first toast flush against the bottom anchor', () => {
    const id1 = addToast()
    setToastHeight(id1, 84)
    expect(getToastOffset(id1)).toBe(0)
  })

  it('offsets each toast by the measured height of the ones below it', () => {
    const id1 = addToast()
    const id2 = addToast()
    const id3 = addToast()
    setToastHeight(id1, 84)
    setToastHeight(id2, 60)
    setToastHeight(id3, 40)
    expect(getToastOffset(id1)).toBe(0)
    expect(getToastOffset(id2)).toBe(84 + GAP)
    expect(getToastOffset(id3)).toBe(84 + GAP + 60 + GAP)
  })

  it('uses real heights so a tall multi-line toast does not overlap the next one', () => {
    const tall = addToast()
    const short = addToast()
    setToastHeight(tall, 84) // taller than the old fixed 68px row stride
    setToastHeight(short, 52)
    expect(getToastOffset(short)).toBeGreaterThanOrEqual(84)
  })

  it('recomputes offsets when a toast below is removed', () => {
    const id1 = addToast()
    const id2 = addToast()
    setToastHeight(id1, 84)
    setToastHeight(id2, 52)
    expect(getToastOffset(id2)).toBe(84 + GAP)
    removeToast(id1)
    expect(getToastOffset(id2)).toBe(0)
  })

  it('ignores toasts that have not reported a height yet', () => {
    addToast()
    const id2 = addToast()
    expect(getToastOffset(id2)).toBe(GAP)
  })

  it('returns 0 for an unknown id', () => {
    expect(getToastOffset('nope')).toBe(0)
  })

  it('does not notify when the reported height is unchanged', () => {
    const id1 = addToast()
    setToastHeight(id1, 84)
    const listener = vi.fn()
    subscribeToasts(listener)
    setToastHeight(id1, 84)
    expect(listener).not.toHaveBeenCalled()
  })
})
