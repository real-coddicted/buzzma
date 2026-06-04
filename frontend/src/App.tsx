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
import { fetchNotifications } from './api/notificationApi'
import { initSSE } from './api/sseClient'
import { clearSession, getAccessToken } from './api/client'
import { useTheme } from './hooks/useTheme'
import type { NavPage, Notification } from './types'

export default function App() {
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const location = useLocation()
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!getAccessToken())

  const validPages = useMemo(() => new Set<string>(['dashboard','campaigns','connections','assignments','deals','feedback','profile','raise-ticket','my-tickets','notifications','claim-review','users']), [])
  const rawPage = location.pathname.replace(/^\//, '') || 'dashboard'
  const activePage: NavPage = validPages.has(rawPage) ? (rawPage as NavPage) : 'dashboard'

  const handleNavigate = useCallback((page: NavPage) => {
    navigate('/' + page)
  }, [navigate])
  const [notifications, setNotifications] = useState<Notification[]>([])

  const handleLogout = useCallback(() => {
    clearSession()
    setIsAuthenticated(false)
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
    fetchNotifications().then(setNotifications)
  }, [])

  const markAllRead = () => setNotifications(prev => prev.map(n => ({ ...n, unread: false })))
  const toggleRead  = (id: string) => setNotifications(prev => prev.map(n => n.id === id ? { ...n, unread: !n.unread } : n))
  const togglePin   = (id: string) => setNotifications(prev => prev.map(n => n.id === id ? { ...n, pinned: !n.pinned } : n))

  if (!isAuthenticated) {
    return <Auth onAuth={() => setIsAuthenticated(true)} />
  }

  return (
    <AppLayout
      theme={theme}
      onToggleTheme={toggleTheme}
      activePage={activePage}
      canGoBack={location.key !== 'default'}
      onNavigate={handleNavigate}
      notifications={notifications}
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
        <Notifications
          notifications={notifications}
          onMarkAllRead={markAllRead}
          onToggleRead={toggleRead}
          onTogglePin={togglePin}
        />
      )}
      {activePage === 'claim-review' && <ClaimReview />}
      {activePage === 'users'         && <Users />}
    </AppLayout>
  )
}
