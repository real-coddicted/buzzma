import { useEffect, useRef } from 'react'
import { subscribeSSE, type RawHandler } from '../api/sseClient'

export function useSSE(eventType: string, onEvent: () => void, payloadFilter?: string): void {
  const onEventRef = useRef(onEvent)
  onEventRef.current = onEvent
/*
  Takes three params:
  - eventType — the SSE event name to listen for (e.g. EVENT_TYPE_REFRESH)
  - onEvent — callback to fire when the event matches
  - payloadFilter — optional, only fires if the event's payload field equals this string
 */
  useEffect(() => {
    // 1. Builds a handler that receives the raw JSON string from the SSE event
    const handler: RawHandler = (data) => {
    // 2. If payloadFilter is set, parses the JSON and skips the callback if payload doesn't match
      if (payloadFilter !== undefined) {
        try {
          const parsed = JSON.parse(data)
          if (parsed?.payload !== payloadFilter) return
        } catch {
          return
        }
      }
      console.log('[SSE] triggering refresh for', eventType, payloadFilter ?? '(no filter)')
      //3. calls onEvent
      onEventRef.current()
    }
    return subscribeSSE(eventType, handler)
  }, [eventType, payloadFilter])
}
