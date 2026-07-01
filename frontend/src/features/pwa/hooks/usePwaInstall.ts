import { useState, useEffect } from 'react'

interface BeforeInstallPromptEvent extends Event {
  readonly platforms: string[]
  readonly userChoice: Promise<{
    outcome: 'accepted' | 'dismissed'
    platform: string
  }>
  prompt(): Promise<void>
}

export function checkIsIOS(): boolean {
  if (typeof window === 'undefined') return false
  const ua = window.navigator.userAgent
  const isIPhone = /iPhone|iPod/.test(ua)
  const isIPad = /iPad/.test(ua) || (window.navigator.platform === 'MacIntel' && window.navigator.maxTouchPoints > 1)
  return isIPhone || isIPad
}

export function checkIsSafari(): boolean {
  if (typeof window === 'undefined') return false
  const ua = window.navigator.userAgent
  const vendor = window.navigator.vendor
  const isApple = /Apple/.test(vendor)
  const isCriOS = /CriOS/.test(ua)
  const isFxiOS = /FxiOS/.test(ua)
  const isEdgiOS = /EdgiOS/.test(ua)
  const isOPiOS = /OPiOS/.test(ua)
  return isApple && !isCriOS && !isFxiOS && !isEdgiOS && !isOPiOS
}

export function checkIsMobile(): boolean {
  if (typeof window === 'undefined') return false
  const ua = window.navigator.userAgent
  return /Mobi|Android|iPhone|iPad|iPod/i.test(ua) || checkIsIOS()
}

export function checkIsStandalone(): boolean {
  if (typeof window === 'undefined') return false
  const isStandaloneMedia = window.matchMedia('(display-mode: standalone)').matches
  const isStandaloneNav = (window.navigator as any).standalone === true
  return isStandaloneMedia || isStandaloneNav
}

export function usePwaInstall() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null)
  const [isDismissed, setIsDismissed] = useState<boolean>(() => {
    return localStorage.getItem('buzzma-pwa-install-dismissed') === 'true'
  })
  const [isStandalone, setIsStandalone] = useState<boolean>(checkIsStandalone)

  useEffect(() => {
    if (typeof window === 'undefined') return

    const mediaQuery = window.matchMedia('(display-mode: standalone)')
    const handleChange = (e: MediaQueryListEvent) => {
      setIsStandalone(e.matches || (window.navigator as any).standalone === true)
    }

    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange)
    } else {
      mediaQuery.addListener(handleChange)
    }

    return () => {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', handleChange)
      } else {
        mediaQuery.removeListener(handleChange)
      }
    }
  }, [])

  useEffect(() => {
    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault()
      setDeferredPrompt(e as BeforeInstallPromptEvent)
    }

    const handleAppInstalled = () => {
      setIsStandalone(true)
      setDeferredPrompt(null)
    }

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt)
    window.addEventListener('appinstalled', handleAppInstalled)

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt)
      window.removeEventListener('appinstalled', handleAppInstalled)
    }
  }, [])

  const installPwa = async () => {
    if (checkIsIOS()) {
      return
    }

    if (!deferredPrompt) return

    try {
      await deferredPrompt.prompt()
      const { outcome } = await deferredPrompt.userChoice

      if (outcome === 'accepted') {
        setIsStandalone(true)
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

  const isIOS = checkIsIOS()
  const isSafari = checkIsSafari()
  const isMobile = checkIsMobile()

  const canInstall = isIOS
    ? (isSafari && !isStandalone)
    : (isMobile && !!deferredPrompt && !isStandalone)

  const showInstallBanner = canInstall && !isDismissed

  return {
    isIOS,
    isStandalone,
    canInstall,
    showInstallBanner,
    installPwa,
    dismissPrompt,
    resetDismissal,
  }
}

