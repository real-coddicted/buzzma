import { useState } from 'react'
import { TurnstileWidget } from '../components/ui/TurnstileWidget'

const SITE_KEY = import.meta.env.VITE_TURNSTILE_SITE_KEY || '1x00000000000000000000AA'

interface CaptchaProps {
  onVerify: (token: string) => void
}

export function Captcha({ onVerify }: CaptchaProps) {
  const [failed, setFailed] = useState(false)

  return (
    <div className="min-h-screen bg-surface-dark-base flex flex-col items-center justify-center gap-4">
      <TurnstileWidget
        siteKey={SITE_KEY}
        onVerify={onVerify}
        onError={() => setFailed(true)}
      />
      {failed && (
        <p className="text-xs text-neon-red">
          Verification failed. Please refresh and try again.
        </p>
      )}
    </div>
  )
}
