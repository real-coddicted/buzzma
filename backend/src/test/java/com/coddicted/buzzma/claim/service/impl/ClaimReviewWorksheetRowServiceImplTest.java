package com.coddicted.buzzma.claim.service.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import com.coddicted.buzzma.claim.persistence.ClaimReviewWorksheetRowRepository;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.math.BigInteger;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimReviewWorksheetRowServiceImplTest {

  private static final UUID ROW_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID WORKSHEET_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final UUID CLAIM_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
  private static final UUID CAMPAIGN_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
  private static final UUID REVIEWER_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
  private static final String CLAIM_CODE = "CLM1-A2B3";
  private static final String ORDER_ID = "403-1234567-8901234";

  @Mock private ClaimReviewWorksheetRowRepository rowRepository;
  @Mock private ClaimReviewWorksheetService worksheetService;
  @Mock private ClaimService claimService;
  @Mock private ClaimReviewService claimReviewService;
  @Mock private CampaignService campaignService;
  @Mock private CampaignShareService campaignShareService;

  private ClaimReviewWorksheetRowServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new ClaimReviewWorksheetRowServiceImpl(
            rowRepository,
            worksheetService,
            claimService,
            claimReviewService,
            campaignService,
            campaignShareService);
  }

  private ClaimReviewWorksheetRow row(final String amountApproved) {
    return ClaimReviewWorksheetRow.builder()
        .id(ROW_ID)
        .worksheetId(WORKSHEET_ID)
        .claimCode(CLAIM_CODE)
        .orderId(ORDER_ID)
        .brandReview("APPROVED")
        .amountApproved(amountApproved)
        .build();
  }

  private void stubReviewableClaimOnCampaign(final CampaignType campaignType) {
    final Claim claim =
        Claim.builder()
            .id(CLAIM_ID)
            .campaignId(CAMPAIGN_ID)
            .ecommerceOrderId(ORDER_ID)
            .status(ClaimStatus.UNDER_REVIEW)
            .build();
    when(claimService.getByCode(CLAIM_CODE)).thenReturn(claim);
    when(worksheetService.getUploadedBy(WORKSHEET_ID)).thenReturn(REVIEWER_ID);
    when(campaignService.getById(CAMPAIGN_ID))
        .thenReturn(
            Campaign.builder().id(CAMPAIGN_ID).ownerId(REVIEWER_ID).type(campaignType).build());
  }

  @Test
  void testAppReviewClaimApprovesWithZeroAmount() {
    stubReviewableClaimOnCampaign(CampaignType.CAMPAIGN_TYPE_APP_REVIEW);

    service.processRow(row("0"));

    verify(claimReviewService)
        .submitClaimReview(
            CLAIM_ID,
            REVIEWER_ID,
            UserRole.ROLE_AGENCY,
            ReviewerDecision.APPROVED,
            null,
            BigInteger.ZERO);
    verify(rowRepository).markSuccess(ROW_ID);
  }

  @Test
  void testAppReviewClaimRejectsNegativeAmount() {
    stubReviewableClaimOnCampaign(CampaignType.CAMPAIGN_TYPE_APP_REVIEW);

    service.processRow(row("-5"));

    verify(rowRepository).markFailed(ROW_ID, "Approved amount cannot be negative");
  }

  @Test
  void testNonAppReviewClaimStillRejectsZeroAmount() {
    stubReviewableClaimOnCampaign(CampaignType.CAMPAIGN_TYPE_ORDER);

    service.processRow(row("0"));

    verify(rowRepository).markFailed(ROW_ID, "Approved amount must be greater than zero");
  }
}
