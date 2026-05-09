import { useState, useEffect } from 'react'
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
import { useTheme } from './hooks/useTheme'
import type { NavPage, Notification } from './types'

export default function App() {
  const { theme, toggleTheme } = useTheme()
  const [activePage, setActivePage] = useState<NavPage>('dashboard')
  const [isAuthenticated, setIsAuthenticated] = useState(import.meta.env.DEV)
  const [notifications, setNotifications] = useState<Notification[]>([])

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
