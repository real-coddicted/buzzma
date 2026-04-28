import { useState } from 'react'
import { AppLayout } from './components/layout/AppLayout'
import { Dashboard } from './pages/Dashboard'
import { Campaigns } from './pages/Campaigns'
import { useTheme } from './hooks/useTheme'
import type { NavPage } from './types'

export default function App() {
  const { theme, toggleTheme } = useTheme()
  const [activePage, setActivePage] = useState<NavPage>('dashboard')

  return (
    <AppLayout
      theme={theme}
      onToggleTheme={toggleTheme}
      activePage={activePage}
      onNavigate={setActivePage}
    >
      {activePage === 'dashboard' && <Dashboard />}
      {activePage === 'campaigns' && <Campaigns />}
    </AppLayout>
  )
}
