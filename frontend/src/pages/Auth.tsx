import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Captcha } from './Captcha'
import { Login } from './Login'
import { Register } from './Register'
import { ForgotPassword } from './ForgotPassword'
import { ResetPassword } from './ResetPassword'

type AuthView = 'captcha' | 'login' | 'register' | 'forgot-password' | 'reset-password'

interface AuthProps {
  onAuth: () => void
}

const PATH_TO_VIEW: Record<string, AuthView> = {
  '/login': 'login',
  '/register': 'register',
  '/forgot-password': 'forgot-password',
  '/reset-password': 'reset-password',
}

export function Auth({ onAuth }: AuthProps) {
  const navigate = useNavigate()
  const location = useLocation()
  const [captchaToken, setCaptchaToken] = useState<string>('')

  const routedView = PATH_TO_VIEW[location.pathname] ?? 'captcha'
  const view: AuthView = routedView !== 'captcha' && !captchaToken ? 'captcha' : routedView

  function handleCaptchaVerify(token: string) {
    setCaptchaToken(token)
    if (routedView === 'captcha') {
      navigate('/login')
    }
  }

  if (view === 'captcha') {
    return <Captcha onVerify={handleCaptchaVerify} />
  }

  if (view === 'login') {
    return (
      <Login
        captchaToken={captchaToken}
        onLogin={onAuth}
        onGoToRegister={() => navigate('/register')}
        onGoToForgotPassword={() => navigate('/forgot-password')}
      />
    )
  }

  if (view === 'register') {
    return (
      <Register
        captchaToken={captchaToken}
        onRegister={() => { setCaptchaToken(''); navigate('/login') }}
        onGoToLogin={() => navigate('/login')}
      />
    )
  }

  if (view === 'forgot-password') {
    return (
      <ForgotPassword
        onSuccess={() => navigate('/reset-password')}
        onGoToLogin={() => navigate('/login')}
      />
    )
  }

  return (
    <ResetPassword
      onSuccess={() => navigate('/login')}
      onGoToLogin={() => navigate('/login')}
    />
  )
}
