interface ToggleSwitchProps {
  checked: boolean
  onChange: (checked: boolean) => void
  disabled?: boolean
  label?: string
  hint?: string
}

export function ToggleSwitch({ checked, onChange, disabled = false, label, hint }: ToggleSwitchProps) {
  return (
    <div className="flex items-center gap-3">
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        disabled={disabled}
        onClick={() => onChange(!checked)}
        className={[
          'relative inline-flex h-5 w-9 flex-shrink-0 rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none',
          disabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer focus:ring-2 focus:ring-neon-blue/40',
          checked ? 'bg-neon-blue' : 'bg-surface-light-hover dark:bg-surface-dark-hover border border-surface-light-border dark:border-surface-dark-border',
        ].join(' ')}
      >
        <span
          className={[
            'pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow ring-0 transition duration-200',
            checked ? 'translate-x-4' : 'translate-x-0',
          ].join(' ')}
        />
      </button>
      {label && (
        <span className="text-xs text-ink-light-primary dark:text-ink-dark-primary font-medium">{label}</span>
      )}
      {hint && (
        <span className="text-[10px] text-ink-light-muted dark:text-ink-dark-muted">{hint}</span>
      )}
    </div>
  )
}