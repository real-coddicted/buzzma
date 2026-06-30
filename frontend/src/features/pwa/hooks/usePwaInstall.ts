import { useState, useEffect } from 'react'

interface BeforeInstallPromptEvent extends Event {
  readonly platforms: string[]
  readonly userChoice: Promise<{
    outcome: 'accepted' | 'dismissed'
    platform: string
  }>
  prompt(): Promise<void>
}

export function usePwaInstall() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null)
  const [isDismissed, setIsDismissed] = useState<boolean>(() => {
    return localStorage.getItem('buzzma-pwa-install-dismissed') === 'true'
  })
  const [isInstalled, setIsInstalled] = useState<boolean>(() => {
    if (typeof window !== 'undefined') {
      return (
        window.matchMedia('(display-mode: standalone)').matches ||
        (window.navigator as any).standalone === true
      )
    }
    return false
  })

  useEffect(() => {
    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault()
      setDeferredPrompt(e as BeforeInstallPromptEvent)
    }

    const handleAppInstalled = () => {
      setIsInstalled(true)
      setDeferredPrompt(null)
      console.log('PWA installed successfully')
    }

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt)
    window.addEventListener('appinstalled', handleAppInstalled)

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt)
      window.removeEventListener('appinstalled', handleAppInstalled)
    }
  }, [])

  const installPwa = async () => {
    if (!deferredPrompt) return

    try {
      await deferredPrompt.prompt()
      const { outcome } = await deferredPrompt.userChoice
      console.log(`User response to install prompt: ${outcome}`)

      if (outcome === 'accepted') {
        setIsInstalled(true)
      }
    } catch (err) {
      console.error('Error during PWA installation:', err)
    } finally {
      setDeferredPrompt(null)
    }
  }

  const dismissPrompt = () => {
    localStorage.setItem('buzzma-pwa-install-dismissed', 'true')
    setIsDismissed(true)
  }

  const resetDismissal = () => {
    localStorage.removeItem('buzzma-pwa-install-dismissed')
    setIsDismissed(false)
  }

  const showInstallBanner = !!deferredPrompt && !isDismissed && !isInstalled

  return {
    showInstallBanner,
    installPwa,
    dismissPrompt,
    resetDismissal,
  }
}

