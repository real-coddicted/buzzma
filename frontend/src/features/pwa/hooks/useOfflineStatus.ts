import { useState, useEffect } from 'react'

export function useOfflineStatus() {
  const [isOffline, setIsOffline] = useState(() => {
    if (typeof navigator !== 'undefined') {
      return !navigator.onLine
    }
    return false
  })

  useEffect(() => {
    const handleOnline = () => {
      setIsOffline(false)
      console.log('App is online. Automatically retrying / reloading...')
      window.location.reload()
    }

    const handleOffline = () => {
      setIsOffline(true)
    }

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    return () => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
    }
  }, [])

  return isOffline
}
