// Keep in sync with backend PasswordPolicyValidator.java
export const PASSWORD_MIN_LENGTH = 8
export const PASSWORD_MAX_LENGTH = 64

export interface PasswordRule {
  id: string
  label: string
  error: string
  test: (password: string) => boolean
}

export const PASSWORD_RULES: PasswordRule[] = [
  {
    id: 'minLength',
    label: `At least ${PASSWORD_MIN_LENGTH} characters`,
    error: `Password must be at least ${PASSWORD_MIN_LENGTH} characters`,
    test: p => p.length >= PASSWORD_MIN_LENGTH,
  },
  {
    id: 'maxLength',
    label: `At most ${PASSWORD_MAX_LENGTH} characters`,
    error: `Password must be at most ${PASSWORD_MAX_LENGTH} characters`,
    test: p => p.length <= PASSWORD_MAX_LENGTH,
  },
  {
    id: 'uppercase',
    label: 'One uppercase letter',
    error: 'Password must contain at least one uppercase letter',
    test: p => /[A-Z]/.test(p),
  },
  {
    id: 'lowercase',
    label: 'One lowercase letter',
    error: 'Password must contain at least one lowercase letter',
    test: p => /[a-z]/.test(p),
  },
  {
    id: 'digit',
    label: 'One digit',
    error: 'Password must contain at least one digit',
    test: p => /[0-9]/.test(p),
  },
  {
    id: 'special',
    label: 'One special character',
    error: 'Password must contain at least one special character',
    test: p => /[^A-Za-z0-9\s]/.test(p),
  },
  {
    id: 'noSpaces',
    label: 'No spaces',
    error: 'Password must not contain spaces',
    test: p => !/\s/.test(p),
  },
]

/** Returns the error for the first failed rule, or undefined when the password meets the policy. */
export function getPasswordError(password: string): string | undefined {
  return PASSWORD_RULES.find(rule => !rule.test(password))?.error
}
