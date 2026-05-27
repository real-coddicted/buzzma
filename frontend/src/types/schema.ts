import type { components } from './api'

// Auth
export type UserSignInRequestDto = components['schemas']['UserSignInRequestDto']
export type UserSignInResponseDto = components['schemas']['UserSignInResponseDto']
export type TokensDto = components['schemas']['TokensDto']
export type UserSummary = components['schemas']['UserSummary']
export type UserRole = NonNullable<UserSummary['role']>
export type UserStatus = NonNullable<UserSummary['status']>
export type UserRegistrationRequestDto = components['schemas']['UserRegistrationRequestDto']
export type RefreshTokenRequestDto = components['schemas']['RefreshTokenRequestDto']
export type PasswordResetRequestDto = components['schemas']['PasswordResetRequestDto']
export type ForgotPasswordLookupRequestDto = components['schemas']['ForgotPasswordLookupRequestDto']

// User settings
export type UserSettingsDto = components['schemas']['UserSettingsDto']

// Security questions
export type SecurityQuestion = components['schemas']['SecurityQuestion']
export type SecurityQuestionWrapper = components['schemas']['SecurityQuestionWrapper']
export type SecurityQuestionRequestDto = components['schemas']['SecurityQuestionRequestDto']
export type SecurityQuestionResponseDto = components['schemas']['SecurityQuestionResponseDto']

// Invites
export type InviteRequestDto = components['schemas']['InviteRequestDto']
export type InviteResponseDto = components['schemas']['InviteResponseDto']
export type ConsumeInviteRequestDto = components['schemas']['ConsumeInviteRequestDto']

// Files
export type FileUploadResponseDto = components['schemas']['FileUploadResponseDto']

// Feedback
export type FeedbackRequestDto = components['schemas']['FeedbackRequestDto']
export type FeedbackResponseDto = components['schemas']['FeedbackResponseDto']
export type FeedbackCategory = NonNullable<FeedbackRequestDto['category']>

// Extraction
export type ExtractionResult = components['schemas']['ExtractionResult']
export type ExtractionPlatform = NonNullable<ExtractionResult['platform']>
export type ExtractionJobResponseDto = components['schemas']['ExtractionJobResponseDto']
export type ExtractionJobStatus = NonNullable<ExtractionJobResponseDto['status']>
export type ValidationError = components['schemas']['ValidationError']

// Connections
export type ConnectionRequestDto = components['schemas']['ConnectionRequestDto']
export type ConnectionResponseDto = components['schemas']['ConnectionResponseDto']
export type ConnectionSummaryResponseDto = components['schemas']['ConnectionSummaryResponseDto']

// Claims
export type ClaimRequestDto = components['schemas']['ClaimRequestDto']
export type ClaimResponseDto = components['schemas']['ClaimResponseDto']
export type ClaimStatus = NonNullable<ClaimResponseDto['status']>
export type ClaimReviewStatus = NonNullable<ClaimResponseDto['reviewStatus']>
export type ClaimScreenshotResponseDto = components['schemas']['ClaimScreenshotResponseDto']
export type ScreenshotType = NonNullable<ClaimScreenshotResponseDto['type']>
export type ScreenshotVerificationStatus = NonNullable<ClaimScreenshotResponseDto['verificationStatus']>
export type ClaimReviewResponseDto = components['schemas']['ClaimReviewResponseDto']
export type PageClaimReviewResponseDto = components['schemas']['PageClaimReviewResponseDto']

// Deals
export type DealResponseDto = components['schemas']['DealResponseDto']
export type PagedDealsResponseDto = components['schemas']['PagedDealsResponseDto']
export type Platform = NonNullable<DealResponseDto['platform']>
export type CampaignType = NonNullable<DealResponseDto['dealType']>

// Campaigns
export type CampaignRequestDto = components['schemas']['CampaignRequestDto']
export type CampaignResponseDto = components['schemas']['CampaignResponseDto']
export type CampaignSummaryResponseDto = components['schemas']['CampaignSummaryResponseDto']
export type CampaignStatus = NonNullable<CampaignResponseDto['status']>
export type CampaignAssignmentRequestDto = components['schemas']['CampaignAssignmentRequestDto']
export type CampaignAssignmentResponseDto = components['schemas']['CampaignAssignmentResponseDto']

// Assignments
export type AssignmentResponseDto = components['schemas']['AssignmentResponseDto']
export type PagedAssignmentsResponseDto = components['schemas']['PagedAssignmentsResponseDto']
export type PublishAssignmentRequestDto = components['schemas']['PublishAssignmentRequestDto']
export type CommissionResponseDto = components['schemas']['CommissionResponseDto']

// Tickets
export type TicketRequestDto = components['schemas']['TicketRequestDto']
export type TicketResponseDto = components['schemas']['TicketResponseDto']
export type TicketStatus = NonNullable<TicketResponseDto['status']>
export type TicketCommentRequestDto = components['schemas']['TicketCommentRequestDto']
export type TicketCommentResponseDto = components['schemas']['TicketCommentResponseDto']
export type TicketAttachmentResponseDto = components['schemas']['TicketAttachmentResponseDto']
export type TicketAssignRequestDto = components['schemas']['TicketAssignRequestDto']
export type TicketStatusUpdateRequestDto = components['schemas']['TicketStatusUpdateRequestDto']
export type TicketCategoryResponseDto = components['schemas']['TicketCategoryResponseDto']
export type TicketSubCategoryResponseDto = components['schemas']['TicketSubCategoryResponseDto']

// Pagination
export type Pageable = components['schemas']['Pageable']
export type PageableObject = components['schemas']['PageableObject']
export type SortObject = components['schemas']['SortObject']
