import { Loading } from '../Loading'
import { ClaimProofCompareTable } from './ClaimProofCompareTable'
import { ClaimProofActions } from './ClaimProofActions'
import { ClaimInfo } from './ClaimInfo'
import { SCREENSHOT_TYPE_CONFIG } from './claimReviewConstants'
import { getProofScore, scorePillClass } from './claimUtils'
import type { ClaimProofItem } from './ClaimProofGallery'
import type { ClaimReviewItem } from '../../../types'

interface Props {
  items: ClaimProofItem[]
  loading: boolean
  activeId: string | null
  sectionRefs: { current: Record<string, HTMLElement | null> }
  claim: ClaimReviewItem
  campaignTitle?: string
  userRole: string | undefined
  onOpenOverlay: (item: ClaimProofItem) => void
  onApproveClaim: (comment: string) => void
  onVerifiedClaim: () => void
  onRejectClaim: (comment: string) => void
}

export function ClaimProofRightPanel({
  items,
  loading,
  activeId,
  sectionRefs,
  claim,
  campaignTitle,
  userRole,
  onOpenOverlay,
  onApproveClaim,
  onVerifiedClaim,
  onRejectClaim,
}: Props) {
  return (
    <div className="flex-1 min-w-0 overflow-y-auto">
      {loading ? (
        <Loading size={32} className="m-auto mt-12" />
      ) : (
        <div className="flex flex-col gap-3 p-3">

          {items.map((item, idx) => {
            const sc = SCREENSHOT_TYPE_CONFIG[item.type ?? '']
            const score = getProofScore(item)

            return (
              <section
                key={item.id}
                ref={el => { sectionRefs.current[item.id] = el }}
                className={[
                  'rounded-xl border overflow-hidden',
                  'bg-surface-light-card dark:bg-surface-dark-card',
                  activeId === item.id
                    ? 'border-neon-blue/40'
                    : 'border-surface-light-border dark:border-surface-dark-border',
                ].join(' ')}
              >
                <div className="flex items-center gap-2.5 px-4 py-2.5 bg-surface-light-raised dark:bg-surface-dark-raised border-b border-surface-light-border dark:border-surface-dark-border">
                  {sc && (
                    <span className={['text-[9px] font-extrabold px-2 py-0.5 rounded-full uppercase tracking-wide flex-shrink-0', sc.tagClass].join(' ')}>
                      {sc.tag}
                    </span>
                  )}
                  <span className="text-sm font-semibold text-ink-light-primary dark:text-ink-dark-primary flex-1">
                    {sc?.label ?? `Screenshot ${idx + 1}`}
                  </span>
                  {item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_VERIFIED' && (
                    <span className="text-sm font-bold px-2 py-0.5 rounded-full bg-neon-green/10 border border-neon-green/30 text-neon-green flex-shrink-0">
                      ✓ Verified
                    </span>
                  )}
                  {item.verificationStatus === 'SCREENSHOT_VERIFICATION_STATUS_REJECTED' && (
                    <span className="text-sm font-bold px-2 py-0.5 rounded-full bg-neon-red/10 border border-neon-red/30 text-neon-red flex-shrink-0">
                      ✗ Rejected
                    </span>
                  )}
                  <span className={['text-sm font-bold px-2.5 py-0.5 rounded-full flex-shrink-0', scorePillClass(score)].join(' ')}>
                    {score}%
                  </span>
                  <button
                    onClick={() => onOpenOverlay(item)}
                    className="flex-shrink-0 flex items-center gap-1 text-[10.5px] font-semibold px-2.5 py-1 rounded-lg border border-surface-light-border dark:border-surface-dark-border text-ink-light-secondary dark:text-ink-dark-secondary hover:border-neon-blue/40 hover:text-neon-blue transition-colors"
                  >
                    ⤢ View
                  </button>
                </div>

                <div className="min-h-[160px] overflow-x-auto">
                  <ClaimProofCompareTable fields={item.fields} />
                </div>
              </section>
            )
          })}

          <div className="flex flex-col gap-3 pt-1">
            <div className="flex items-center gap-3">
              <div className="h-px flex-1 bg-surface-light-border dark:bg-surface-dark-border" />
              <span className="text-[10px] font-bold text-ink-light-muted dark:text-ink-dark-muted uppercase tracking-widest">
                Claim Administration
              </span>
              <div className="h-px flex-1 bg-surface-light-border dark:bg-surface-dark-border" />
            </div>
            <ClaimInfo claim={claim} campaignTitle={campaignTitle} />
            <ClaimProofActions
              userRole={userRole}
              isUnderReview={claim.isUnderReview ?? false}
              mediatorVerified={claim.mediatorVerified ?? false}
              onApprove={onApproveClaim}
              onVerified={onVerifiedClaim}
              onReject={onRejectClaim}
            />
          </div>

        </div>
      )}
    </div>
  )
}
