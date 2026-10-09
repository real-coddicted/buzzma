import { PASSWORD_RULES } from '../../utils/passwordPolicy'
import { IconCheck } from './icons'

interface PasswordRequirementsProps {
  password: string
}

/** Live checklist of password policy rules for the given value. */
export function PasswordRequirements({ password }: PasswordRequirementsProps) {
  return (
    <ul className="mt-2 grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1">
      {PASSWORD_RULES.map(rule => {
        const met = password.length > 0 && rule.test(password)
        return (
          <li
            key={rule.id}
            className={
              'flex items-center gap-1.5 text-xs transition-colors ' +
              (met ? 'text-neon-green' : 'text-ink-light-muted dark:text-ink-dark-muted')
            }
          >
            {met ? (
              <IconCheck size={12} />
            ) : (
              <span className="inline-block w-3 text-center">-</span>
            )}
            {rule.label}
          </li>
        )
      })}
    </ul>
  )
}
