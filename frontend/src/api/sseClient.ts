import { getAccessToken } from './client'

const SSE_URL = '/api/events/stream'

export type RawHandler = (data: string) => void
type WrappedHandler = (e: Event) => void

let es: EventSource | null = null
const subscriptions = new Map<string, Map<RawHandler, WrappedHandler>>()

export function initSSE(): () => void {
  const token = getAccessToken()
  if (!token) return () => {}

  if (es && es.readyState !== EventSource.CLOSED) {
    es.close()
  }

  const url = `${SSE_URL}?access_token=${encodeURIComponent(token)}`
  es = new EventSource(url)
  console.log('[SSE] connecting to', SSE_URL)

  es.onopen = () => console.log('[SSE] connection opened')

  for (const [eventType, handlers] of subscriptions.entries()) {
    for (const wrapped of handlers.values()) {
      es.addEventListener(eventType, wrapped)
    }
  }

  es.onerror = (e) => {
    console.warn('[SSE] connection error', e)
    es?.close()
    es = null
  }

  return () => {
    es?.close()
    es = null
  }
}

export function subscribeSSE(eventType: string, handler: RawHandler): () => void {
  if (!subscriptions.has(eventType)) subscriptions.set(eventType, new Map())
  const wrapped: WrappedHandler = (e) => {
    const data = (e as MessageEvent).data ?? ''
    console.log('[SSE] event received:', eventType, data)
    handler(data)
  }
  subscriptions.get(eventType)!.set(handler, wrapped)
  es?.addEventListener(eventType, wrapped)

  return () => {
    const wrapped = subscriptions.get(eventType)?.get(handler)
    if (wrapped) {
      es?.removeEventListener(eventType, wrapped)
      subscriptions.get(eventType)?.delete(handler)
    }
  }
}
