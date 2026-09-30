import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { submitSocialStep, createSocialPageClaim } from '../../../api/claimApi'
import { ScreenshotStep } from './ScreenshotStep'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface FollowStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function FollowStep({ deal, claimId, onSuccess, readOnly, claimResponse, rejectedScreenshot }: FollowStepProps) {
  return (
    <ScreenshotStep
      claimId={claimId}
      onSuccess={onSuccess}
      readOnly={readOnly}
      claimResponse={claimResponse}
      rejectedScreenshot={rejectedScreenshot}
      screenshotType="SCREENSHOT_TYPE_FOLLOW"
      label="Follow"
      description={`Follow the account on ${deal.platformLabel} and upload a screenshot showing you are following.`}
      hint="Ensure the follow button shows the following state."
      buttonLabel="Submit Follow"
      buttonColor="bg-neon-blue hover:brightness-110"
      onSubmit={(claimId, file) => submitSocialStep(claimId, file, 'FOLLOW')}
      onCreate={claimId ? undefined : (file) => createSocialPageClaim({
        campaignId: deal.campaignId, dealId: deal.id, stepType: 'FOLLOW', screenshot: file,
      })}
    />
  )
}
