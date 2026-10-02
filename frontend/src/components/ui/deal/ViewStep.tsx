import type { Deal } from '../../../types/DealTypes'
import type { components } from '../../../types/api'
import { submitSocialStep, createSocialPageClaim } from '../../../api/claimApi'
import { ScreenshotStep } from './ScreenshotStep'

type ClaimResponseDto = components['schemas']['ClaimResponseDto']
type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']

interface ViewStepProps {
  deal: Deal
  claimId?: string
  onSuccess: (claim: ClaimResponseDto) => void
  readOnly?: boolean
  claimResponse?: ClaimResponseDto
  rejectedScreenshot?: ClaimScreenshotResponseDto
}

export function ViewStep({ deal, claimId, onSuccess, readOnly, claimResponse, rejectedScreenshot }: ViewStepProps) {
  return (
    <ScreenshotStep
      claimId={claimId}
      onSuccess={onSuccess}
      readOnly={readOnly}
      claimResponse={claimResponse}
      rejectedScreenshot={rejectedScreenshot}
      screenshotType="SCREENSHOT_TYPE_VIEW"
      label="View"
      description={`View the content on ${deal.platformLabel} and upload a screenshot showing the content page.`}
      hint="Ensure the content title and your account name are visible."
      buttonLabel="Submit View"
      buttonColor="bg-neon-blue hover:brightness-110"
      onSubmit={(claimId, file) => submitSocialStep(claimId, file, 'VIEW')}
      onCreate={claimId ? undefined : (file) => createSocialPageClaim({
        campaignId: deal.campaignId, dealId: deal.id, stepType: 'VIEW', screenshot: file,
      })}
    />
  )
}
