import { useState } from 'react'
import { IconCopy, IconCopyCheck } from './icons'

interface CopyableCodeProps {
  code: string
}

export function CopyableCode({ code }: CopyableCodeProps) {
  const [copied, setCopied] = useState(false)

  function handleCopy() {
    navigator.clipboard.writeText(code).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  return (
    <button
      onClick={handleCopy}
      title={copied ? 'Copied!' : 'Copy code'}
      className={[
        'inline-flex items-center gap-1.5 rounded-full border px-2 py-0.5 font-mono text-xs tracking-wide transition-colors',
        copied
          ? 'border-neon-green/40 bg-neon-green/10 text-neon-green'
          : 'border-neon-blue/30 bg-neon-blue/10 text-neon-blue hover:border-neon-blue/50',
      ].join(' ')}
    >
      {code}
      {copied ? <IconCopyCheck size={12} /> : <IconCopy size={12} />}
    </button>
  )
}
