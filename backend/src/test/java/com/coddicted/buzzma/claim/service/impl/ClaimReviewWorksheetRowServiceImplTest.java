package com.coddicted.buzzma.claim.service.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.persistence.ClaimReviewWorksheetRowRepository;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetService;
import com.coddicted.buzzma.claim.service.ClaimService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimReviewWorksheetRowServiceImplTest {

  @Mock private ClaimReviewWorksheetRowRepository rowRepository;
  @Mock private ClaimReviewWorksheetService worksheetService;
  @Mock private ClaimService claimService;
  @Mock private ClaimReviewService claimReviewService;
  @Mock private CampaignService campaignService;
  @Mock private CampaignShareService campaignShareService;

  private ClaimReviewWorksheetRowServiceImpl service() {
    return new ClaimReviewWorksheetRowServiceImpl(
        rowRepository,
        worksheetService,
        claimService,
        claimReviewService,
        campaignService,
        campaignShareService);
  }

  @Test
  void processRowRejectsClaimThatIsReadyForAccountingAsAlreadyProcessed() {
    final UUID rowId = UUID.randomUUID();
    final ClaimReviewWorksheetRow row =
        ClaimReviewWorksheetRow.builder()
            .id(rowId)
            .worksheetId(UUID.randomUUID())
            .claimCode("CLM-1")
            .orderId("ORD-1")
            .brandReview("APPROVED")
            .amountApproved("100.00")
            .build();

    final Claim claim =
        Claim.builder().ecommerceOrderId("ORD-1").status(ClaimStatus.READY_FOR_ACCOUNTING).build();
    when(claimService.getByCode("CLM-1")).thenReturn(claim);

    service().processRow(row);

    verify(rowRepository).markFailed(rowId, "Claim already processed earlier");
  }
}
