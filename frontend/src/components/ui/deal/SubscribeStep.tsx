import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { submitSocialStep, createSocialPageClaim } from '../../../api/claimApi'
import { ScreenshotStep } from './ScreenshotStep'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface SubscribeStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function SubscribeStep({ deal, claimId, onSuccess, readOnly, claimResponse, rejectedScreenshot }: SubscribeStepProps) {
  return (
    <ScreenshotStep
      claimId={claimId}
      onSuccess={onSuccess}
      readOnly={readOnly}
      claimResponse={claimResponse}
      rejectedScreenshot={rejectedScreenshot}
      screenshotType="SCREENSHOT_TYPE_SUBSCRIBE"
      label="Subscribe"
      description={`Subscribe to the channel on ${deal.platformLabel} and upload a screenshot showing you are subscribed.`}
      hint="Ensure the subscribe button shows the subscribed state."
      buttonLabel="Submit Subscribe"
      buttonColor="bg-neon-red hover:brightness-110"
      onSubmit={(claimId, file) => submitSocialStep(claimId, file, 'SUBSCRIBE')}
      onCreate={claimId ? undefined : (file) => createSocialPageClaim({
        campaignId: deal.campaignId, dealId: deal.id, stepType: 'SUBSCRIBE', screenshot: file,
      })}
    />
  )
}
