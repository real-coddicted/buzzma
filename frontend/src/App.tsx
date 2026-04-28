import { useState } from 'react'
import { AppLayout } from './components/layout/AppLayout'
import { Dashboard } from './pages/Dashboard'
import { Campaigns } from './pages/Campaigns'
import { Feedback } from './pages/Feedback'
import { Auth } from './pages/Auth'
import { useTheme } from './hooks/useTheme'
import type { NavPage } from './types'

export default function App() {
  const { theme, toggleTheme } = useTheme()
  const [activePage, setActivePage] = useState<NavPage>('dashboard')
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  if (!isAuthenticated) {
    return <Auth onAuth={() => setIsAuthenticated(true)} />
  }

  return (
    <AppLayout
      theme={theme}
      onToggleTheme={toggleTheme}
      activePage={activePage}
      onNavigate={setActivePage}
    >
      {activePage === 'dashboard' && <Dashboard />}
      {activePage === 'campaigns' && <Campaigns />}
      {activePage === 'feedback' && <Feedback />}
    </AppLayout>
  )
}
