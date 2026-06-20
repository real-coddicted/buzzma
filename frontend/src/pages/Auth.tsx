import { useState } from 'react'
import { Captcha } from './Captcha'
import { Login } from './Login'
import { Register } from './Register'
import { ForgotPassword } from './ForgotPassword'
import { ResetPassword } from './ResetPassword'

type AuthView = 'captcha' | 'login' | 'register' | 'forgot-password' | 'reset-password'

interface AuthProps {
  onAuth: () => void
}

export function Auth({ onAuth }: AuthProps) {
  const [view, setView] = useState<AuthView>('captcha')
  const [captchaToken, setCaptchaToken] = useState<string>('')

  function handleCaptchaVerify(token: string) {
    setCaptchaToken(token)
    setView('login')
  }

  if (view === 'captcha') {
    return <Captcha onVerify={handleCaptchaVerify} />
  }

  if (view === 'login') {
    return (
      <Login
        captchaToken={captchaToken}
        onLogin={onAuth}
        onGoToRegister={() => setView('register')}
        onGoToForgotPassword={() => setView('forgot-password')}
        onCaptchaExpired={() => { setCaptchaToken(''); setView('captcha') }}
      />
    )
  }

  if (view === 'register') {
    return (
      <Register
        captchaToken={captchaToken}
        onRegister={() => { setCaptchaToken(''); setView('captcha') }}
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
