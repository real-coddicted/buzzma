import { LabeledField } from './LabeledField'
import { CopyableCode } from './CopyableCode'
import type { UserDetails } from '../../types/ProfileTypes'

interface DetailsCardProps {
  details: UserDetails
}

const nameLabels: Record<string, string> = {
  brand:    'Brand Name',
  agency:   'Agency Name',
  mediator: 'Mediator Name',
  buyer:    'Buyer Name',
}

export function DetailsCard({ details }: DetailsCardProps) {
  const { code, type, name, mobile, email } = details

  return (
    <div className="rounded-xl border border-surface-light-border dark:border-surface-dark-border bg-surface-light-card dark:bg-surface-dark-card shadow-card-light dark:shadow-card-dark p-5">
      <div className="flex items-center justify-between mb-5">
        <h3 className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary">
          Account Details
        </h3>
        {type && (
          <span className="text-xs font-medium px-2.5 py-1 rounded-full border border-neon-blue/30 bg-neon-blue/10 text-neon-blue capitalize">
            {type}
          </span>
        )}
      </div>

      <div className="space-y-3">
        {code && (
          <>
            <div className="flex flex-col gap-1">
              <span className="text-xs font-medium text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-wide">
                Code
              </span>
              <CopyableCode code={code} />
            </div>
            <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
          </>
        )}
        <LabeledField
          label={type ? (nameLabels[type] ?? 'Name') : 'Name'}
          value={name}
        />
        <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
        <LabeledField label="Mobile" value={mobile} />
        <div className="border-t border-surface-light-border dark:border-surface-dark-border" />
        <LabeledField label="Email" value={email ?? ''} />
      </div>
    </div>
  )
}
