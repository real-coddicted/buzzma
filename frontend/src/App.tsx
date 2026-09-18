import { useEffect, useCallback, useState, useMemo, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { AppLayout } from './components/layout/AppLayout'
import { Dashboard } from './pages/Dashboard'
import { Campaigns } from './pages/Campaigns'
import { Connections } from './pages/Connections'
import { Assignments } from './pages/Assignments'
import { Deals } from './pages/Deals'
import { MyClaims } from './pages/MyClaims'
import { Feedback } from './pages/Feedback'
import { Profile } from './pages/Profile'
import { RaiseTicket } from './pages/RaiseTicket'
import { MyTickets } from './pages/MyTickets'
import { Notifications } from './pages/Notifications'
import { ClaimReview } from './pages/ClaimReview'
import { Users } from './pages/Users'
import { MyPayments } from './pages/MyPayments'
import { UserPayouts } from './pages/UserPayouts'
import { Auth } from './pages/Auth'
import { TermsPage } from './pages/TermsPage'
import { TermsReacceptDialog } from './components/ui/TermsReacceptDialog'
import { Loading } from './components/ui/Loading'
import { Toast } from './components/ui/Toast'
import { fetchUnreadNotificationCount } from './api/notificationApi'
import { fetchAllTickets } from './api/ticketApi'
import { fetchUserSettings } from './api/userSettingsApi'
import { fetchTermsAcceptanceStatus } from './api/termsApi'
import { initSSE } from './api/sseClient'
import { useSSE } from './hooks/useSSE'
import { cancelProactiveRefresh, clearSession, getAccessToken } from './api/client'
import { signOut } from './api/authApi'
import { useTheme } from './hooks/useTheme'
import { isPageVisible, getFirstEnabledPage } from './utils/tabRedirect'
import { isAuthPath, POST_LOGIN_REDIRECT_KEY } from './utils/postLoginRedirect'
import { runAfterMinDuration } from './utils/minDuration'
import type { NavPage } from './types'
import type { components } from './types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

/** Minimum time (ms) to keep the post-login spinner visible, so it doesn't flash instantly on fast connections. */
const MIN_POST_LOGIN_LOADING_MS = 300

export default function App() {
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const location = useLocation()
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!getAccessToken())
  const [userSettings, setUserSettings] = useState<UserSettingsDto | null>(null)
  const [mustReacceptTerms, setMustReacceptTerms] = useState(false)
  const [userSettingsLoading, setUserSettingsLoading] = useState(true)
  const [termsStatusLoading, setTermsStatusLoading] = useState(true)
  const [userSettingsError, setUserSettingsError] = useState<string | null>(null)
  const [termsError, setTermsError] = useState<string | null>(null)
  const loginTimestampRef = useRef(Date.now())

  const validPages = useMemo(() => new Set<NavPage>(['dashboard','campaigns','connections','assignments','deals','my-claims','feedback','profile','raise-ticket','my-tickets','notifications','claim-review','users','tickets','my-payments','user-payouts']), [])
  const rawPage = location.pathname.replace(/^\//, '') || 'dashboard'
  const activePage: NavPage = validPages.has(rawPage as NavPage) ? (rawPage as NavPage) : 'dashboard'

  const handleNavigate = useCallback((page: NavPage) => {
    navigate('/' + page)
  }, [navigate])
  const [unreadNotificationCount, setUnreadNotificationCount] = useState(0)

  const handleLogout = useCallback(async () => {
    cancelProactiveRefresh()
    try { await signOut() } catch { /* network down — still clear local state */ }
    clearSession()
    setIsAuthenticated(false)
    setUserSettings(null)
    navigate('/login')
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
    let cancelled = false
    setUserSettingsLoading(true)
    const settled = fetchUserSettings()
      .then(settings => { if (!cancelled) setUserSettings(settings) })
      .catch(err => {
        console.error(err)
        if (!cancelled) setUserSettingsError('Failed to load your account settings. Some tabs may be unavailable until this is retried.')
      })
    const cancelMinDuration = runAfterMinDuration(settled, loginTimestampRef, MIN_POST_LOGIN_LOADING_MS, () => setUserSettingsLoading(false))
    return () => { cancelled = true; cancelMinDuration() }
  }, [isAuthenticated])

  useEffect(() => {
    if (!isAuthenticated) return
    let cancelled = false
    setTermsStatusLoading(true)
    const settled = fetchTermsAcceptanceStatus()
      .then(status => { if (!cancelled) setMustReacceptTerms(!!status.mustReaccept) })
      .catch(err => {
        console.error(err)
        if (!cancelled) setTermsError('Failed to load terms status. Please retry.')
      })
    const cancelMinDuration = runAfterMinDuration(settled, loginTimestampRef, MIN_POST_LOGIN_LOADING_MS, () => setTermsStatusLoading(false))
    return () => { cancelled = true; cancelMinDuration() }
  }, [isAuthenticated])

  useEffect(() => {
    if (!isAuthenticated) return
    if (userSettingsLoading) return
    if (isPageVisible(activePage, userSettings)) return
    navigate('/' + (userSettings ? getFirstEnabledPage(userSettings) : 'feedback'), { replace: true })
  }, [isAuthenticated, userSettings, userSettingsLoading, activePage, navigate])

  useEffect(() => {
    if (isAuthenticated) return
    if (isAuthPath(location.pathname)) return
    sessionStorage.setItem(POST_LOGIN_REDIRECT_KEY, location.pathname + location.search)
  }, [isAuthenticated, location.pathname, location.search])

  useEffect(() => {
    if (!isAuthenticated) return
    fetchUnreadNotificationCount().then(setUnreadNotificationCount).catch(console.error)
  }, [isAuthenticated])

  useSSE('EVENT_TYPE_NOTIFICATION', () => {
    fetchUnreadNotificationCount().then(setUnreadNotificationCount).catch(console.error)
  })

  if (location.pathname === '/terms') {
    return <TermsPage />
  }

  if (!isAuthenticated) {
    return (
      <Auth
        onAuth={() => {
          const redirectTo = sessionStorage.getItem(POST_LOGIN_REDIRECT_KEY)
          sessionStorage.removeItem(POST_LOGIN_REDIRECT_KEY)
          loginTimestampRef.current = Date.now()
          setUserSettings(null)
          setUserSettingsLoading(true)
          setTermsStatusLoading(true)
          setUserSettingsError(null)
          setTermsError(null)
          setIsAuthenticated(true)
          if (redirectTo) navigate(redirectTo, { replace: true })
        }}
      />
    )
  }

  const errorToasts = (
    <>
      {userSettingsError && (
        <Toast
          message={userSettingsError}
          type="error"
          actionLabel="Retry"
          onAction={() => window.location.reload()}
          onDismiss={() => setUserSettingsError(null)}
        />
      )}
      {termsError && (
        <Toast
          message={termsError}
          type="error"
          actionLabel="Retry"
          onAction={() => window.location.reload()}
          onDismiss={() => setTermsError(null)}
        />
      )}
    </>
  )

  if (userSettingsLoading || termsStatusLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loading size={32} />
        {errorToasts}
      </div>
    )
  }

  if (mustReacceptTerms) {
    return <TermsReacceptDialog onAccepted={() => setMustReacceptTerms(false)} />
  }

  return (
    <AppLayout
      theme={theme}
      onToggleTheme={toggleTheme}
      activePage={activePage}
      onNavigate={handleNavigate}
      unreadNotificationCount={unreadNotificationCount}
      userSettings={userSettings}
    >
      {activePage === 'dashboard'     && <Dashboard />}
      {activePage === 'campaigns'     && <Campaigns />}
      {activePage === 'connections'    && <Connections />}
      {activePage === 'assignments'  && <Assignments />}
      {activePage === 'deals'          && <Deals />}
      {activePage === 'my-claims'     && <MyClaims />}
      {activePage === 'feedback'      && <Feedback />}
      {activePage === 'profile'       && <Profile />}
      {activePage === 'raise-ticket'  && <RaiseTicket />}
      {activePage === 'my-tickets'      && <MyTickets />}
      {activePage === 'notifications' && (
        <Notifications onUnreadCountChange={setUnreadNotificationCount} />
      )}
      {activePage === 'claim-review' && <ClaimReview />}
      {activePage === 'users'         && <Users />}
      {activePage === 'tickets' && <MyTickets title="Tickets" fetchFn={fetchAllTickets} />}
      {activePage === 'my-payments'  && <MyPayments />}
      {activePage === 'user-payouts' && <UserPayouts />}
      {errorToasts}
    </AppLayout>
  )
}
