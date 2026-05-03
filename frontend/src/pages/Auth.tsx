import { useState } from 'react'
import { Login } from './Login'
import { Register } from './Register'
import { ForgotPassword } from './ForgotPassword'
import { ResetPassword } from './ResetPassword'

type AuthView = 'login' | 'register' | 'forgot-password' | 'reset-password'

interface AuthProps {
  onAuth: () => void
}

export function Auth({ onAuth }: AuthProps) {
  const [view, setView] = useState<AuthView>('login')

  if (view === 'login') {
    return (
      <Login
        onLogin={onAuth}
        onGoToRegister={() => setView('register')}
        onGoToForgotPassword={() => setView('forgot-password')}
      />
    )
  }

  if (view === 'register') {
    return (
      <Register
        onRegister={() => setView('login')}
        onGoToLogin={() => setView('login')}
      />
    )
  }

  if (view === 'forgot-password') {
    return (
      <ForgotPassword
        onSuccess={() => setView('reset-password')}
        onGoToLogin={() => setView('login')}
      />
    )
  }

  return (
    <ResetPassword
      onSuccess={() => setView('login')}
      onGoToLogin={() => setView('login')}
    />
  )
}
