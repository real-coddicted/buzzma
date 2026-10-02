import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { submitSocialStep, createSocialPageClaim } from '../../../api/claimApi'
import { ScreenshotStep } from './ScreenshotStep'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface LikeStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function LikeStep({ deal, claimId, onSuccess, readOnly, claimResponse, rejectedScreenshot }: LikeStepProps) {
  return (
    <ScreenshotStep
      claimId={claimId}
      onSuccess={onSuccess}
      readOnly={readOnly}
      claimResponse={claimResponse}
      rejectedScreenshot={rejectedScreenshot}
      screenshotType="SCREENSHOT_TYPE_LIKE"
      label="Like"
      description={`Like the content on ${deal.platformLabel} and upload a screenshot showing the liked state (solid thumbs-up or filled heart).`}
      hint="Ensure the like button appears solid/filled, not outlined."
      buttonLabel="Submit Like"
      buttonColor="bg-neon-pink hover:brightness-110"
      onSubmit={(claimId, file) => submitSocialStep(claimId, file, 'LIKE')}
      onCreate={claimId ? undefined : (file) => createSocialPageClaim({
        campaignId: deal.campaignId, dealId: deal.id, stepType: 'LIKE', screenshot: file,
      })}
    />
  )
}
