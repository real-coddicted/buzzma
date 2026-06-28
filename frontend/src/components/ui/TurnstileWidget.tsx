import { useEffect, useRef } from 'react'

interface Props {
  siteKey: string
  onVerify: (token: string) => void
  onError?: () => void
  theme?: 'light' | 'dark' | 'auto'
  appearance?: 'always' | 'execute' | 'interaction-only'
}

export function TurnstileWidget({ siteKey, onVerify, onError, theme = 'dark', appearance = 'always' }: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const widgetIdRef = useRef<string | null>(null)
  const onVerifyRef = useRef(onVerify)
  const onErrorRef = useRef(onError)

  onVerifyRef.current = onVerify
  onErrorRef.current = onError

  useEffect(() => {
    const container = containerRef.current
    if (!container) return

    let active = true

    function render() {
      if (!active || !container || !window.turnstile) return
      if (widgetIdRef.current) return
      widgetIdRef.current = window.turnstile.render(container, {
        sitekey: siteKey,
        theme,
        appearance,
        callback: (token: string) => {
          if (active) onVerifyRef.current(token)
        },
        'error-callback': () => {
          if (active) onErrorRef.current?.()
        },
        'expired-callback': () => {
          if (active && widgetIdRef.current) {
            window.turnstile?.reset(widgetIdRef.current)
          }
        },
      })
    }

    if (window.turnstile) {
      render()
    } else {
      const script = document.querySelector<HTMLScriptElement>('script[src*="turnstile"]')
      script?.addEventListener('load', render)
      return () => {
        active = false
        script?.removeEventListener('load', render)
      }
    }

    return () => {
      active = false
      if (widgetIdRef.current && window.turnstile) {
        window.turnstile.remove(widgetIdRef.current)
        widgetIdRef.current = null
      }
    }
  }, [siteKey, theme, appearance])

  return <div ref={containerRef} />
}
