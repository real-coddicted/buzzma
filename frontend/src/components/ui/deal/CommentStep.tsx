import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { submitSocialStep, createSocialPageClaim } from '../../../api/claimApi'
import { ScreenshotStep } from './ScreenshotStep'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface CommentStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function CommentStep({ deal, claimId, onSuccess, readOnly, claimResponse, rejectedScreenshot }: CommentStepProps) {
  return (
    <ScreenshotStep
      claimId={claimId}
      onSuccess={onSuccess}
      readOnly={readOnly}
      claimResponse={claimResponse}
      rejectedScreenshot={rejectedScreenshot}
      screenshotType="SCREENSHOT_TYPE_COMMENT"
      label="Comment"
      description={`Comment on the content on ${deal.platformLabel} and upload a screenshot showing your published comment.`}
      hint="Ensure your username and comment text are clearly visible."
      buttonLabel="Submit Comment"
      buttonColor="bg-neon-green hover:brightness-110"
      onSubmit={(claimId, file) => submitSocialStep(claimId, file, 'COMMENT')}
      onCreate={claimId ? undefined : (file) => createSocialPageClaim({
        campaignId: deal.campaignId, dealId: deal.id, stepType: 'COMMENT', screenshot: file,
      })}
    />
  )
}
