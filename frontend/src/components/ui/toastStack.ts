/** Shared registry so multiple mounted <Toast> instances stack instead of overlapping. */

type Listener = () => void

interface ToastEntry {
  id: string
  /** Measured height in px; 0 until the instance reports its rendered size. */
  height: number
}

/** Vertical gap (px) between stacked toasts. */
const TOAST_GAP_PX = 12

let entries: ToastEntry[] = []
let nextId = 0
const listeners = new Set<Listener>()

function notify(): void {
  listeners.forEach(l => l())
}

export function addToast(): string {
  const id = `toast-${++nextId}`
  entries = [...entries, { id, height: 0 }]
  notify()
  return id
}

export function removeToast(id: string): void {
  entries = entries.filter(entry => entry.id !== id)
  notify()
}

/** Records a toast's rendered height so toasts below it can be offset by the real amount. */
export function setToastHeight(id: string, height: number): void {
  const current = entries.find(entry => entry.id === id)
  if (!current || current.height === height) return
  entries = entries.map(entry => (entry.id === id ? { ...entry, height } : entry))
  notify()
}

export function getToastIndex(id: string): number {
  return entries.findIndex(entry => entry.id === id)
}

/** Distance (px) this toast sits above the bottom anchor, clearing all toasts stacked below it. */
export function getToastOffset(id: string): number {
  const index = getToastIndex(id)
  if (index < 0) return 0
  return entries
    .slice(0, index)
    .reduce((sum, entry) => sum + entry.height + TOAST_GAP_PX, 0)
}

export function subscribeToasts(listener: Listener): () => void {
  listeners.add(listener)
  return () => { listeners.delete(listener) }
}

/** Test-only: reset the shared stack between test cases. */
export function __resetToastStackForTests(): void {
  entries = []
  nextId = 0
  listeners.clear()
}
