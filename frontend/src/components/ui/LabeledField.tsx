import { useState } from 'react'
import { IconCopy, IconCopyCheck, IconEye, IconEyeOff } from './icons'

interface LabeledFieldProps {
  label: string
  value: string
  mono?: boolean
  copyable?: boolean
  masked?: boolean
}

const MASK = '••••••••'

export function LabeledField({ label, value, mono = false, copyable = false, masked = false }: LabeledFieldProps) {
  const [copied, setCopied] = useState(false)
  const [revealed, setRevealed] = useState(false)

  function handleCopy() {
    navigator.clipboard.writeText(value).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  const displayValue = masked && !revealed ? MASK : (value || '—')

  return (
    <div className="flex flex-col gap-4">
      <span className="text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide">
        {label}
      </span>
      <div className="flex items-center gap-2">
        <span className={[
          'text-sm text-ink-light-primary dark:text-ink-dark-primary',
          mono ? 'font-mono tracking-widest' : '',
        ].join(' ')}>
          {displayValue}
        </span>
        {masked && value && (
          <button
            onClick={() => setRevealed(r => !r)}
            title={revealed ? 'Hide' : 'Reveal'}
            className="flex items-center justify-center w-6 h-6 rounded-md border transition-colors border-surface-light-border dark:border-surface-dark-border text-ink-light-muted dark:text-ink-dark-muted hover:border-neon-blue/40 hover:text-neon-blue"
          >
            {revealed ? <IconEyeOff size={12} /> : <IconEye size={12} />}
          </button>
        )}
        {copyable && value && (
          <button
            onClick={handleCopy}
            title={copied ? 'Copied!' : 'Copy'}
            className={[
              'flex items-center justify-center w-6 h-6 rounded-md border transition-colors',
              copied
                ? 'border-neon-green/40 bg-neon-green/10 text-neon-green'
                : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-muted dark:text-ink-dark-muted hover:border-neon-blue/40 hover:text-neon-blue',
            ].join(' ')}
          >
            {copied ? <IconCopyCheck size={12} /> : <IconCopy size={12} />}
          </button>
        )}
      </div>
    </div>
  )
}
