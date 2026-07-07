import { useEffect, useCallback, useState, useMemo } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { AppLayout } from './components/layout/AppLayout'
import { Dashboard } from './pages/Dashboard'
import { Campaigns } from './pages/Campaigns'
import { Connections } from './pages/Connections'
import { Assignments } from './pages/Assignments'
import { Deals } from './pages/Deals'
import { Feedback } from './pages/Feedback'
import { Profile } from './pages/Profile'
import { RaiseTicket } from './pages/RaiseTicket'
import { MyTickets } from './pages/MyTickets'
import { Notifications } from './pages/Notifications'
import { ClaimReview } from './pages/ClaimReview'
import { Users } from './pages/Users'
import { Auth } from './pages/Auth'
import { fetchUnreadNotificationCount } from './api/notificationApi'
import { fetchAllTickets } from './api/ticketApi'
import { fetchUserSettings } from './api/userSettingsApi'
import { initSSE } from './api/sseClient'
import { cancelProactiveRefresh, clearSession, getAccessToken } from './api/client'
import { useTheme } from './hooks/useTheme'
import { isTabDisabled, getFirstEnabledPage } from './utils/tabRedirect'
import type { NavPage } from './types'
import type { components } from './types/api'

type UserSettingsDto = components['schemas']['UserSettingsDto']

export default function App() {
  const { theme, toggleTheme } = useTheme()
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
  const [unreadNotificationCount, setUnreadNotificationCount] = useState(0)

  const handleLogout = useCallback(() => {
    cancelProactiveRefresh()
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
    fetchUnreadNotificationCount().then(setUnreadNotificationCount).catch(console.error)
  }, [isAuthenticated])

  if (!isAuthenticated) {
    return <Auth onAuth={() => setIsAuthenticated(true)} />
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
    </AppLayout>
  )
}
