import { useState } from 'react'
import { fetchDealByCode } from '../../../api/dealApi'
import { buildWhatsAppMessage } from '../../../utils/whatsappMessage'
import { getCurrentUser } from '../../../api/client'
import { IconWhatsApp, IconCopyCheck } from '../icons'
import { Loading } from '../Loading'

interface Props {
  dealCode: string
  stopPropagation?: boolean
}

export function ShareOnWhatsAppButton({ dealCode, stopPropagation = false }: Props) {
  const [state, setState] = useState<'idle' | 'loading' | 'copied'>('idle')

  async function handleShare(e: React.MouseEvent) {
    if (stopPropagation) e.stopPropagation()
    setState('loading')
    const deal = await fetchDealByCode(dealCode)
    const mediatorName = getCurrentUser()?.name ?? ''
    await navigator.clipboard.writeText(buildWhatsAppMessage(deal, mediatorName))
    setState('copied')
    setTimeout(() => setState('idle'), 2000)
  }

  return (
    <button
      title={state === 'copied' ? 'Copied!' : 'Share on WhatsApp'}
      onClick={handleShare}
      disabled={state === 'loading'}
      className={[
        'p-1.5 rounded-lg transition-colors',
        state === 'copied' ? 'text-neon-green bg-neon-green/10' : 'text-whatsapp bg-whatsapp/10 hover:bg-whatsapp/20',
      ].join(' ')}
    >
      {state === 'loading' && <Loading size={14} />}
      {state === 'copied' && <IconCopyCheck size={14} />}
      {state === 'idle' && <IconWhatsApp size={14} />}
    </button>
  )
}
