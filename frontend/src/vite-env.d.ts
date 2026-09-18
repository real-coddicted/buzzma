/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_TURNSTILE_SITE_KEY: string
  readonly VITE_APP_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

interface TurnstileRenderOptions {
  sitekey: string
  callback?: (token: string) => void
  'error-callback'?: () => void
  'expired-callback'?: () => void
  theme?: 'light' | 'dark' | 'auto'
  appearance?: 'always' | 'execute' | 'interaction-only'
  size?: 'normal' | 'compact'
}

interface Window {
  __APP_BASE_URL__?: string
  turnstile?: {
    render(container: string | HTMLElement, options: TurnstileRenderOptions): string
    reset(widgetId: string): void
    remove(widgetId: string): void
    getResponse(widgetId: string): string | undefined
  }
}
