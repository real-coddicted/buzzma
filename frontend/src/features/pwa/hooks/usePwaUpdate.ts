import { useRegisterSW } from 'virtual:pwa-register/react'

export function usePwaUpdate() {
  const {
    needRefresh: [needRefresh, setNeedRefresh],
    updateServiceWorker,
  } = useRegisterSW({
    onRegistered(r) {
      console.log('Service Worker registered successfully:', r)
    },
    onRegisterError(error) {
      console.error('Service Worker registration failed:', error)
    },
  })

  const updateApp = () => {
    updateServiceWorker(true)
  }

  const dismissUpdate = () => {
    setNeedRefresh(false)
  }

  return {
    updateAvailable: needRefresh,
    updateApp,
    dismissUpdate,
  }
}
