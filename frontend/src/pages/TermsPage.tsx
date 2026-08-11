import { useState } from 'react'
import { APP_NAME } from '../constants/app'
import { Toast } from '../components/ui/Toast'
import { AuthBackground } from '../components/ui/AuthBackground'
import { TermsAndConditions } from '../components/ui/TermsAndConditions'

export function TermsPage() {
  const [error, setError] = useState<string | null>(null)

  return (
    <div className="min-h-screen bg-surface-light-base dark:bg-surface-dark-base flex items-start justify-center p-4 py-10 relative overflow-hidden">
      <AuthBackground variant="green" />

      {error && <Toast message={error} type="error" onDismiss={() => setError(null)} />}

      <div className="w-full relative z-10">
        <div className="text-center mb-6">
          <span className="text-2xl font-bold text-neon-green tracking-tight">{APP_NAME}</span>
        </div>

        <div className="rounded-2xl border border-neon-green/20 bg-surface-light-card dark:bg-surface-dark-card shadow-neon-green p-6">
          <TermsAndConditions onError={setError} />
        </div>
      </div>
    </div>
  )
}