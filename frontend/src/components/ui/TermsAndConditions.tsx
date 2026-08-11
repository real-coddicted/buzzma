import { useEffect, useState } from 'react'
import { fetchTerms } from '../../api/termsApi'

interface TermsAndConditionsProps {
  onError?: (message: string) => void
}

export function TermsAndConditions({ onError }: TermsAndConditionsProps) {
  const [content, setContent] = useState('')

  useEffect(() => {
    fetchTerms()
      .then(terms => setContent(terms.content ?? ''))
      .catch((err: unknown) => {
        onError?.(err instanceof Error ? err.message : 'Failed to load terms and conditions.')
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div
      className={
        'text-sm text-ink-light-secondary dark:text-ink-dark-secondary ' +
        '[&_h1]:text-lg [&_h1]:font-bold [&_h1]:mb-2 ' +
        '[&_h2]:text-sm [&_h2]:font-semibold [&_h2]:mt-4 [&_h2]:mb-1 ' +
        '[&_p]:mb-2 ' +
        '[&_ul]:list-disc [&_ul]:pl-5 [&_ul]:mb-2 [&_ul]:space-y-1'
      }
      dangerouslySetInnerHTML={{ __html: content || 'Loading terms and conditions…' }}
    />
  )
}