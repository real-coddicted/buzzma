package com.coddicted.buzzma.claim.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppPromotionClaimCreationTemplateTest {

  private static final UUID CAMPAIGN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID DEAL_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID SLOT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID OWNER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID CLAIM_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID SCREENSHOT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

  private static final String CLAIM_CODE = "CLM1-A2B3";
  private static final String SCREENSHOT_KEY = "claims/screenshot.jpg";
  private static final String SCREENSHOT_FILENAME = "screenshot.jpg";
  private static final String CONTENT_TYPE = "image/jpeg";
  private static final byte[] SCREENSHOT_BYTES = {1, 2, 3};

  private static final Claim CLAIM_INPUT =
      Claim.builder().campaignId(CAMPAIGN_ID).dealId(DEAL_ID).ownerId(OWNER_ID).build();

  private static final Deal DEAL_WITH_SLOT =
      Deal.builder().campaignSlot(CampaignSlot.builder().id(SLOT_ID).build()).build();

  private static final Campaign ACTIVE_CAMPAIGN =
      Campaign.builder()
          .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
          .platform(Platform.PLATFORM_GOOGLE_PLAY_STORE)
          .build();

  private static final Campaign CLOSED_CAMPAIGN =
      Campaign.builder().status(CampaignStatus.CAMPAIGN_STATUS_CLOSED).build();

  @Mock private DealService mockDealService;
  @Mock private CampaignService mockCampaignService;
  @Mock private CampaignSlotService mockCampaignSlotService;
  @Mock private CampaignStepResolver mockCampaignStepResolver;
  @Mock private StorageService mockStorageService;
  @Mock private ExtractionService mockExtractionService;
  @Mock private CodeGenerationService mockCodeGenerationService;
  @Mock private ClaimService mockClaimService;

  private AppPromotionClaimCreationTemplate template;

  @BeforeEach
  void setUp() {
    this.template =
        new AppPromotionClaimCreationTemplate(
            this.mockDealService,
            this.mockCampaignService,
            this.mockCampaignSlotService,
            this.mockCampaignStepResolver,
            this.mockStorageService,
            this.mockExtractionService,
            this.mockCodeGenerationService,
            this.mockClaimService);
  }

  @Test
  void testCreateWhenFirstStepIsDownloadInstall() {
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_WITH_SLOT);
    when(this.mockCampaignService.getById(CAMPAIGN_ID)).thenReturn(ACTIVE_CAMPAIGN);
    when(this.mockCampaignSlotService.decrementSlot(SLOT_ID)).thenReturn(1);
    when(this.mockCampaignStepResolver.resolve(ACTIVE_CAMPAIGN))
        .thenReturn(List.of(CampaignStepType.DOWNLOAD_INSTALL, CampaignStepType.CASHBACK));
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CLAIM))
        .thenReturn(CLAIM_CODE);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture()))
        .thenReturn(CLAIM_INPUT.toBuilder().id(CLAIM_ID).ownerId(OWNER_ID).build());
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    when(this.mockClaimService.saveScreenshot(screenshotCaptor.capture()))
        .thenReturn(ClaimScreenshot.builder().id(SCREENSHOT_ID).build());

    final Claim result =
        this.template.create(
            CLAIM_INPUT, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE, null, null);

    assertEquals(CLAIM_ID, result.getId());
    final Claim saved = claimCaptor.getValue();
    assertEquals(ClaimStatus.DOWNLOADED_AND_INSTALLED, saved.getStatus());
    assertEquals("NA", saved.getEcommerceOrderId());
    assertEquals(Platform.PLATFORM_GOOGLE_PLAY_STORE, saved.getPlatform());
    assertEquals(CampaignStepType.DOWNLOAD_INSTALL, saved.getCurrentStep());
    assertEquals(CLAIM_CODE, saved.getCode());

    assertEquals(
        ScreenshotType.SCREENSHOT_TYPE_DOWNLOAD_INSTALL, screenshotCaptor.getValue().getType());
    verify(this.mockExtractionService).submitJob(SCREENSHOT_ID, OWNER_ID);
  }

  @Test
  void testCreateWhenFirstStepIsReview() {
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_WITH_SLOT);
    when(this.mockCampaignService.getById(CAMPAIGN_ID)).thenReturn(ACTIVE_CAMPAIGN);
    when(this.mockCampaignSlotService.decrementSlot(SLOT_ID)).thenReturn(1);
    when(this.mockCampaignStepResolver.resolve(ACTIVE_CAMPAIGN))
        .thenReturn(List.of(CampaignStepType.REVIEW, CampaignStepType.CASHBACK));
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CLAIM))
        .thenReturn(CLAIM_CODE);
    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    when(this.mockClaimService.save(claimCaptor.capture()))
        .thenReturn(CLAIM_INPUT.toBuilder().id(CLAIM_ID).ownerId(OWNER_ID).build());
    when(this.mockStorageService.store(
            "claims", SCREENSHOT_FILENAME, CONTENT_TYPE, SCREENSHOT_BYTES))
        .thenReturn(SCREENSHOT_KEY);
    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    when(this.mockClaimService.saveScreenshot(screenshotCaptor.capture()))
        .thenReturn(ClaimScreenshot.builder().id(SCREENSHOT_ID).build());

    this.template.create(
        CLAIM_INPUT, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE, null, null);

    assertEquals(ClaimStatus.REVIEW_SUBMITTED, claimCaptor.getValue().getStatus());
    assertEquals(CampaignStepType.REVIEW, claimCaptor.getValue().getCurrentStep());
    assertEquals(ScreenshotType.SCREENSHOT_TYPE_REVIEW, screenshotCaptor.getValue().getType());
  }

  @Test
  void testCreateWhenCampaignNotActiveThrows() {
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_WITH_SLOT);
    when(this.mockCampaignService.getById(CAMPAIGN_ID)).thenReturn(CLOSED_CAMPAIGN);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.template.create(
                    CLAIM_INPUT, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE, null, null));

    assertEquals(
        "The campaign is not active anymore. Please go back to deals page and refresh once to"
            + " confirm active deals",
        ex.getMessage());
    verify(this.mockCampaignSlotService, never()).decrementSlot(SLOT_ID);
    verifyNoInteractions(this.mockClaimService);
  }

  @Test
  void testCreateWhenSlotsExhaustedThrows() {
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_WITH_SLOT);
    when(this.mockCampaignService.getById(CAMPAIGN_ID)).thenReturn(ACTIVE_CAMPAIGN);
    when(this.mockCampaignSlotService.decrementSlot(SLOT_ID)).thenReturn(0);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.template.create(
                    CLAIM_INPUT, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE, null, null));

    assertEquals("All slots have been claimed for this deal", ex.getMessage());
    verifyNoInteractions(this.mockClaimService);
  }

  @Test
  void testCreateWhenFirstStepIsUnexpectedThrows() {
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL_WITH_SLOT);
    when(this.mockCampaignService.getById(CAMPAIGN_ID)).thenReturn(ACTIVE_CAMPAIGN);
    when(this.mockCampaignSlotService.decrementSlot(SLOT_ID)).thenReturn(1);
    when(this.mockCampaignStepResolver.resolve(ACTIVE_CAMPAIGN))
        .thenReturn(List.of(CampaignStepType.RATING, CampaignStepType.CASHBACK));

    assertThrows(
        IllegalStateException.class,
        () ->
            this.template.create(
                CLAIM_INPUT, SCREENSHOT_BYTES, SCREENSHOT_FILENAME, CONTENT_TYPE, null, null));
  }

  @Test
  void testCategoryIsAppPromotion() {
    assertEquals(PromotionCategory.APP_PROMOTION, this.template.category());
  }
}
