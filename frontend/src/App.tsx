import { useEffect, useCallback, useState, useMemo, lazy, Suspense } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { AppLayout } from './components/layout/AppLayout'
import { Auth } from './pages/Auth'
import { Loading } from './components/ui/Loading'

const Dashboard = lazy(() => import('./pages/Dashboard').then(m => ({ default: m.Dashboard })))
const Campaigns = lazy(() => import('./pages/Campaigns').then(m => ({ default: m.Campaigns })))
const Connections = lazy(() => import('./pages/Connections').then(m => ({ default: m.Connections })))
const Assignments = lazy(() => import('./pages/Assignments').then(m => ({ default: m.Assignments })))
const Deals = lazy(() => import('./pages/Deals').then(m => ({ default: m.Deals })))
const Feedback = lazy(() => import('./pages/Feedback').then(m => ({ default: m.Feedback })))
const Profile = lazy(() => import('./pages/Profile').then(m => ({ default: m.Profile })))
const RaiseTicket = lazy(() => import('./pages/RaiseTicket').then(m => ({ default: m.RaiseTicket })))
const MyTickets = lazy(() => import('./pages/MyTickets').then(m => ({ default: m.MyTickets })))
const Notifications = lazy(() => import('./pages/Notifications').then(m => ({ default: m.Notifications })))
const ClaimReview = lazy(() => import('./pages/ClaimReview').then(m => ({ default: m.ClaimReview })))
const Users = lazy(() => import('./pages/Users').then(m => ({ default: m.Users })))
import { fetchNotifications, markAsRead, markAsUnread, pinNotification, markAllRead as apiMarkAllRead } from './api/notificationApi'
import { fetchAllTickets } from './api/ticketApi'
import { fetchUserSettings } from './api/userSettingsApi'
import { initSSE } from './api/sseClient'
import { clearSession, getAccessToken } from './api/client'
import { useTheme } from './hooks/useTheme'
import { usePwaUpdate, InstallPwaBanner, usePwaInstall, useOfflineStatus, OfflinePage, UpdateNotification } from './features/pwa'
import { isTabDisabled, getFirstEnabledPage } from './utils/tabRedirect'
import type { NavPage, Notification } from './types'
import type { components } from './types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

const PageLoader = () => (
  <div className="flex items-center justify-center min-h-[300px] w-full">
    <Loading size={32} />
  </div>
)

export default function App() {
  const { theme, toggleTheme } = useTheme()
  usePwaUpdate()
  const { showInstallBanner, installPwa, dismissPrompt, resetDismissal } = usePwaInstall()
  const isOffline = useOfflineStatus()
  const [isOfflineDismissed, setIsOfflineDismissed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!getAccessToken())
  const [userSettings, setUserSettings] = useState<UserSettingsDto | null>(null)

  const validPages = useMemo(() => new Set<string>(['dashboard','campaigns','connections','assignments','deals','feedback','profile','raise-ticket','my-tickets','notifications','claim-review','users','tickets']), [])
  const rawPage = location.pathname.replace(/^\//, '') || 'dashboard'
  const activePage: NavPage = validPages.has(rawPage) ? (rawPage as NavPage) : 'dashboard'

  const handleNavigate = useCallback((page: NavPage) => {
    navigate('/' + page)
  }, [navigate])
  const [notifications, setNotifications] = useState<Notification[]>([])

  const handleLogout = useCallback(() => {
    clearSession()
    setIsAuthenticated(false)
    setUserSettings(null)
    navigate('/dashboard')
  }, [navigate])

  useEffect(() => {
    window.addEventListener('auth:logout', handleLogout)
    return () => window.removeEventListener('auth:logout', handleLogout)
  }, [handleLogout])

  useEffect(() => {
    if (!isAuthenticated) return
    return initSSE()
  }, [isAuthenticated])

  useEffect(() => {
    if (!isAuthenticated) return
    fetchUserSettings().then(setUserSettings).catch(console.error)
  }, [isAuthenticated])

  useEffect(() => {
    if (!userSettings) return
    if (isTabDisabled(activePage, userSettings)) {
      navigate('/' + getFirstEnabledPage(userSettings), { replace: true })
    }
  }, [userSettings, activePage, navigate])

  useEffect(() => {
    if (!isAuthenticated) return
    fetchNotifications().then(setNotifications).catch(console.error)
  }, [isAuthenticated])

  const markAllRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, unread: false })))
    apiMarkAllRead().catch(console.error)
  }

  const toggleRead = (id: string) => {
    setNotifications(prev => prev.map(n => {
      if (n.id !== id) return n
      const next = { ...n, unread: !n.unread }
      ;(next.unread ? markAsUnread : markAsRead)(id).catch(console.error)
      return next
    }))
  }

  const togglePin = (id: string) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, pinned: !n.pinned } : n))
    pinNotification(id).catch(console.error)
  }

  if (isOffline && !isOfflineDismissed) {
    return <OfflinePage onDismiss={() => setIsOfflineDismissed(true)} />
  }

  if (!isAuthenticated) {
    return (
      <Auth
        onAuth={() => {
          resetDismissal()
          setIsAuthenticated(true)
        }}
      />
    )
  }

  return (
    <>
      <AppLayout
        theme={theme}
        onToggleTheme={toggleTheme}
        activePage={activePage}
        onNavigate={handleNavigate}
        notifications={notifications}
        userSettings={userSettings}
      >
        <Suspense fallback={<PageLoader />}>
          {activePage === 'dashboard'     && <Dashboard />}
          {activePage === 'campaigns'     && <Campaigns />}
          {activePage === 'connections'    && <Connections />}
          {activePage === 'assignments'  && <Assignments />}
          {activePage === 'deals'          && <Deals />}
          {activePage === 'feedback'      && <Feedback />}
          {activePage === 'profile'       && <Profile />}
          {activePage === 'raise-ticket'  && <RaiseTicket />}
          {activePage === 'my-tickets'      && <MyTickets />}
          {activePage === 'notifications' && (
            <Notifications
              notifications={notifications}
              onMarkAllRead={markAllRead}
              onToggleRead={toggleRead}
              onTogglePin={togglePin}
            />
          )}
          {activePage === 'claim-review' && <ClaimReview />}
          {activePage === 'users'         && <Users />}
          {activePage === 'tickets' && <MyTickets title="Tickets" fetchFn={fetchAllTickets} />}
        </Suspense>
      </AppLayout>
      <InstallPwaBanner
        showInstallBanner={showInstallBanner}
        installPwa={installPwa}
        dismissPrompt={dismissPrompt}
      />
      <UpdateNotification />
    </>
  )
}
