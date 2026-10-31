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
import { Card } from './components/ui/Card'
import { ViewOnlyProvider } from './contexts/ViewOnlyContext'
import { Toast } from './components/ui/Toast'
import { ViewAsPicker } from './components/ui/ViewAsPicker'
import { ViewAsBanner } from './components/ui/ViewAsBanner'
import { ROLE_CONFIG } from './components/ui/users/UserListItem'
import { VIEW_AS_ROLES } from './config/viewAsRoles'
import { fetchUnreadNotificationCount } from './api/notificationApi'
import { fetchAllTickets } from './api/ticketApi'
import { fetchUserSettings } from './api/userSettingsApi'
import { fetchTermsAcceptanceStatus } from './api/termsApi'
import { initSSE } from './api/sseClient'
import { useSSE } from './hooks/useSSE'
import { cancelProactiveRefresh, clearSession, getAccessToken, getSignedInUser, getViewAsUserId, setViewAsUserId, setViewAsUser as storeViewAsUser } from './api/client'
import { fetchUserById, type UserSummaryDto } from './api/userApi'
import { signOut } from './api/authApi'
import { useTheme } from './hooks/useTheme'
import { useViewAsUserId } from './hooks/useViewAs'
import { readViewAsParam, withViewAsParam } from './utils/viewAs'
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
  // The view-as user (null = the admin themselves) the loaded settings belong to.
  const [userSettingsFor, setUserSettingsFor] = useState<string | null>(null)
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
    navigate({ pathname: '/' + page, search: withViewAsParam('', getViewAsUserId()) })
  }, [navigate])
  const viewAsUserId = useViewAsUserId()

  // ?viewAs in the URL selects the user an admin views as. Pages that rewrite their query
  // string drop it, so put it back rather than silently leaving view-as mode.
  useEffect(() => {
    if (!isAuthenticated) return
    const fromUrl = readViewAsParam(location.search)
    if (fromUrl) {
      setViewAsUserId(fromUrl)
      return
    }
    const current = getViewAsUserId()
    if (current) {
      navigate({ ...location, search: withViewAsParam(location.search, current) }, { replace: true })
    }
  }, [isAuthenticated, location, navigate])

  const [viewAsUser, setViewAsUser] = useState<UserSummaryDto | null>(null)
  const [viewAsError, setViewAsError] = useState<string | null>(null)

  // Both entering and leaving view-as go back to the page's root view, since a detail view
  // open for one user means nothing for another.
  const handleViewAs = useCallback((user: UserSummaryDto) => {
    if (!user.id) return
    storeViewAsUser(user)
    setViewAsUser(user)
    setViewAsUserId(user.id)
    navigate({ pathname: location.pathname, search: withViewAsParam('', user.id) })
  }, [navigate, location.pathname])

  const handleExitViewAs = useCallback(() => {
    setViewAsUserId(null)
    navigate({ pathname: location.pathname, search: '' })
  }, [navigate, location.pathname])

  useEffect(() => {
    if (!viewAsUserId) {
      setViewAsUser(null)
      return
    }
    if (viewAsUser?.id === viewAsUserId) return
    let cancelled = false
    fetchUserById(viewAsUserId, true)
      .then(user => {
        if (cancelled) return
        if (user.role === 'ROLE_ADMIN') throw new Error('Cannot view as another admin.')
        storeViewAsUser(user)
        setViewAsUser(user)
      })
      .catch(err => {
        if (cancelled) return
        console.error(err)
        setViewAsError(`Could not view as this user. ${(err as Error).message}`)
        setViewAsUserId(null)
        navigate({ pathname: window.location.pathname, search: '' }, { replace: true })
      })
    return () => { cancelled = true }
  }, [viewAsUserId, viewAsUser, navigate])
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
    // The stream carries the admin's own events, which mean nothing while viewing as someone.
    if (!isAuthenticated || viewAsUserId) return
    return initSSE()
  }, [isAuthenticated, viewAsUserId])

  useEffect(() => {
    if (!isAuthenticated) return
    let cancelled = false
    setUserSettingsLoading(true)
    const settled = fetchUserSettings()
      .then(settings => {
        if (cancelled) return
        setUserSettings(settings)
        setUserSettingsFor(viewAsUserId)
      })
      .catch(err => {
        console.error(err)
        if (cancelled) return
        setUserSettings(null)
        setUserSettingsFor(viewAsUserId)
        setUserSettingsError('Failed to load your account settings. Some tabs may be unavailable until this is retried.')
      })
    const cancelMinDuration = runAfterMinDuration(settled, loginTimestampRef, MIN_POST_LOGIN_LOADING_MS, () => setUserSettingsLoading(false))
    return () => { cancelled = true; cancelMinDuration() }
    // Refetched on view-as changes: the request is served as the viewed user, so the
    // sidebar shows their tabs.
  }, [isAuthenticated, viewAsUserId])

  useEffect(() => {
    if (!isAuthenticated) return
    // The viewed user's terms are theirs to accept; the admin's own are checked again on exit.
    if (viewAsUserId) {
      setMustReacceptTerms(false)
      setTermsStatusLoading(false)
      return
    }
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
  }, [isAuthenticated, viewAsUserId])

  useEffect(() => {
    if (!isAuthenticated) return
    if (userSettingsLoading) return
    // Settings still belong to the previous view-as user until the refetch lands.
    if (userSettingsFor !== viewAsUserId) return
    // While viewing as someone, stay put: a tab hidden for them is shown as such instead.
    if (viewAsUserId) return
    if (isPageVisible(activePage, userSettings)) return
    navigate('/' + (userSettings ? getFirstEnabledPage(userSettings) : 'feedback'), { replace: true })
  }, [isAuthenticated, userSettings, userSettingsFor, userSettingsLoading, activePage, navigate, viewAsUserId])

  useEffect(() => {
    if (isAuthenticated) return
    if (isAuthPath(location.pathname)) return
    sessionStorage.setItem(POST_LOGIN_REDIRECT_KEY, location.pathname + location.search)
  }, [isAuthenticated, location.pathname, location.search])

  useEffect(() => {
    if (!isAuthenticated) return
    fetchUnreadNotificationCount().then(setUnreadNotificationCount).catch(console.error)
  }, [isAuthenticated, viewAsUserId])

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
      {viewAsError && (
        <Toast message={viewAsError} type="error" onDismiss={() => setViewAsError(null)} />
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
      topbarActions={getSignedInUser()?.role === 'ROLE_ADMIN' && VIEW_AS_ROLES[activePage].length > 0 && (
        <ViewAsPicker
          roles={VIEW_AS_ROLES[activePage]}
          selectedName={viewAsUser?.name}
          onSelect={handleViewAs}
        />
      )}
      banner={viewAsUserId && (
        <ViewAsBanner
          name={viewAsUser?.name ?? 'Loading…'}
          roleLabel={viewAsUser?.role ? ROLE_CONFIG[viewAsUser.role]?.label : undefined}
          onExit={handleExitViewAs}
        />
      )}
    >
      {/* Keyed on the viewed user so switching users remounts the page and refetches. */}
      {/* Pages read the viewed user's role, so wait for their profile before rendering. */}
      {viewAsUserId && viewAsUser?.id !== viewAsUserId ? (
        <div className="flex justify-center py-20"><Loading size={32} /></div>
      ) : viewAsUserId && !isPageVisible(activePage, userSettings) ? (
        <Card>
          <p className="text-sm text-ink-light-secondary dark:text-ink-dark-secondary">
            This tab is disabled for {viewAsUser?.name ?? 'this user'}.
          </p>
        </Card>
      ) : (
      <ViewOnlyProvider key={viewAsUserId ?? 'self'} viewOnly={!!viewAsUserId}>
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
      </ViewOnlyProvider>
      )}
      {errorToasts}
    </AppLayout>
  )
}
