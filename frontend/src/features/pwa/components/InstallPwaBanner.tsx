import { Button } from '../../../components/ui/Button'
import { getCurrentUser } from '../../../api/client'

interface InstallPwaBannerProps {
  showInstallBanner: boolean
  installPwa: () => Promise<void>
  dismissPrompt: () => void
}

export function InstallPwaBanner({ showInstallBanner, installPwa, dismissPrompt }: InstallPwaBannerProps) {
  const currentUser = getCurrentUser()
  const isBuyer = currentUser?.role === 'ROLE_BUYER'

  if (!showInstallBanner || !isBuyer) return null

  return (
    <div className="fixed bottom-4 left-4 right-4 md:left-auto md:right-4 md:max-w-md z-[100] animate-slide-in" role="dialog" aria-labelledby="install-banner-title" aria-describedby="install-banner-desc">
      <div className="bg-surface-light-raised dark:bg-surface-dark-raised border border-surface-light-border dark:border-surface-dark-border rounded-xl shadow-xl p-4 flex items-start gap-4 backdrop-blur-md bg-opacity-95 dark:bg-opacity-95">
        {/* PWA Icon resembling favicon.svg */}
        <div className="w-12 h-12 rounded-xl flex-shrink-0 bg-gradient-to-br from-[#57c7ff] to-[#bd93f9] flex items-center justify-center shadow-lg" aria-hidden="true">
          <span className="text-xl font-black text-surface-dark-base select-none">B</span>
        </div>

        {/* Content */}
        <div className="flex-grow">
          <h4 id="install-banner-title" className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
            Install Buzzma
          </h4>
          <p id="install-banner-desc" className="text-xs text-ink-light-secondary dark:text-ink-dark-secondary mt-1">
            Install our app to get a faster, offline-capable dashboard experience.
          </p>
          <div className="flex items-center gap-2 mt-3">
            <Button variant="primary" size="sm" onClick={installPwa} aria-label="Install Buzzma App">
              Install
            </Button>
            <Button variant="ghost" size="sm" onClick={dismissPrompt} aria-label="Dismiss install prompt">
              Later
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}
