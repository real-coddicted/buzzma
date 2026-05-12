import { useState, useEffect, useCallback } from 'react'
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
import { OrderReview } from './pages/OrderReview'
import { Auth } from './pages/Auth'
import { fetchNotifications } from './api/notificationApi'
import { getAccessToken } from './api/client'
import { useTheme } from './hooks/useTheme'
import type { NavPage, Notification } from './types'

export default function App() {
  const { theme, toggleTheme } = useTheme()
  const [activePage, setActivePage] = useState<NavPage>('dashboard')
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!getAccessToken())
  const [notifications, setNotifications] = useState<Notification[]>([])

  const handleLogout = useCallback(() => {
    setIsAuthenticated(false)
    setActivePage('dashboard')
  }, [])

  useEffect(() => {
    window.addEventListener('auth:logout', handleLogout)
    return () => window.removeEventListener('auth:logout', handleLogout)
  }, [handleLogout])

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
      onNavigate={setActivePage}
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
      {activePage === 'order-review' && <OrderReview />}
    </AppLayout>
  )
}
