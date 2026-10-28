import { useState } from 'react'
import { Navigate, useNavigate, useLocation } from 'react-router-dom'
import { Captcha } from './Captcha'
import { Login } from './Login'
import { Register } from './Register'
import { ForgotPassword } from './ForgotPassword'
import { ResetPassword } from './ResetPassword'
import type { ForgotPasswordForm } from '../types/ForgotPasswordTypes'

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
  // kept across both pages so going back from reset shows what was entered
  const [forgotForm, setForgotForm] = useState<ForgotPasswordForm | null>(null)

  const routedView = PATH_TO_VIEW[location.pathname] ?? 'captcha'
  const view: AuthView = routedView !== 'captcha' && !captchaToken ? 'captcha' : routedView

  function handleCaptchaVerify(token: string) {
    setCaptchaToken(token)
    if (routedView === 'captcha') {
      navigate('/login', { replace: true })
    }
  }

  if (view === 'captcha') {
    return <Captcha onVerify={handleCaptchaVerify} />
  }

  if (view === 'login') {
    return (
      <Login
        initialCaptchaToken={captchaToken}
        onLogin={onAuth}
        onGoToRegister={() => navigate('/register')}
        onGoToForgotPassword={() => navigate('/forgot-password')}
      />
    )
  }

  if (view === 'register') {
    return (
      <Register
        initialCaptchaToken={captchaToken}
        onRegister={() => { setCaptchaToken(''); navigate('/login') }}
        onGoToLogin={() => navigate('/login')}
      />
    )
  }

  if (view === 'forgot-password') {
    return (
      <ForgotPassword
        initialForm={forgotForm ?? undefined}
        onSuccess={form => { setForgotForm(form); navigate('/reset-password') }}
        onGoToLogin={() => navigate('/login')}
      />
    )
  }

  if (!forgotForm) {
    return <Navigate to="/forgot-password" replace />
  }

  return (
    <ResetPassword
      challenge={{ mobile: forgotForm.mobile, questionId: forgotForm.questionId, answer: forgotForm.answer }}
      onSuccess={() => { setForgotForm(null); navigate('/login') }}
      onGoToLogin={() => navigate('/login')}
      onBack={() => navigate('/forgot-password')}
    />
  )
}
