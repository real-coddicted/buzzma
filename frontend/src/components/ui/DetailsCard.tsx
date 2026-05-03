import { useState } from 'react'
import { IconCopy, IconCopyCheck } from './icons'
import type { UserDetails } from '../../types/ProfileTypes'

interface DetailsCardProps {
  details: UserDetails
}

interface FieldProps {
  label: string
  value: string
  mono?: boolean
}

function Field({ label, value, mono = false }: FieldProps) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide">
        {label}
      </span>
      <span className={[
        'text-sm text-ink-light-primary dark:text-ink-dark-primary',
        mono ? 'font-mono tracking-widest' : '',
      ].join(' ')}>
        {value}
      </span>
    </div>
  )
}

function CopyableCode({ code }: { code: string }) {
  const [copied, setCopied] = useState(false)

  function handleCopy() {
    navigator.clipboard.writeText(code).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide">
        Code
      </span>
      <div className="flex items-center gap-2">
        <span className="text-sm font-mono tracking-widest text-ink-light-primary dark:text-ink-dark-primary">
          {code}
        </span>
        <button
          onClick={handleCopy}
          title={copied ? 'Copied!' : 'Copy code'}
          className={[
            'flex items-center justify-center w-6 h-6 rounded-md border transition-colors',
            copied
              ? 'border-neon-green/40 bg-neon-green/10 text-neon-green'
              : 'border-surface-light-border dark:border-surface-dark-border text-ink-light-muted dark:text-ink-dark-muted hover:border-neon-blue/40 hover:text-neon-blue',
          ].join(' ')}
        >
          {copied ? <IconCopyCheck size={12} /> : <IconCopy size={12} />}
        </button>
      </div>
    </div>
  )
}

export function DetailsCard({ details }: DetailsCardProps) {
  const { code, type, name, mobile } = details

  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
      <div className="flex items-center justify-between mb-5">
        <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          Account Details
        </h3>
        <span className="text-xs font-medium px-2.5 py-1 rounded-full border border-neon-blue/30 bg-neon-blue/10 text-neon-blue capitalize">
          {type}
        </span>
      </div>

      <div className="space-y-4">
        <CopyableCode code={code} />
        <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
        <Field
          label={type === 'brand' ? 'Brand Name' : 'Agency Name'}
          value={name}
        />
        <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
        <Field label="Mobile" value={mobile} />
      </div>
    </div>
  )
}
