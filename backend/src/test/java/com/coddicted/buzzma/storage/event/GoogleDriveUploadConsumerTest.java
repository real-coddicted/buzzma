package com.coddicted.buzzma.storage.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.storage.config.GoogleDriveProperties;
import com.coddicted.buzzma.storage.service.GoogleDriveService;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class GoogleDriveUploadConsumerTest {

  private static final UUID SCREENSHOT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CLAIM_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CAMPAIGN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID OWNER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final String STORAGE_KEY = "claims/abc123.jpg";
  private static final String CAMPAIGN_CODE = "CAM001";
  private static final String CLAIM_CODE = "CLM001";
  private static final String ROOT_FOLDER_ID = "root-folder-123";
  private static final String CAMPAIGN_FOLDER_ID = "campaign-folder-456";
  private static final String CLAIM_FOLDER_ID = "claim-folder-789";
  private static final String DRIVE_URL = "https://drive.google.com/file/d/abc123/view";
  private static final byte[] FILE_BYTES = {1, 2, 3, 4, 5};

  @Mock private StringRedisTemplate mockRedisTemplate;
  @Mock private ClaimScreenshotRepository mockScreenshotRepository;
  @Mock private ClaimRepository mockClaimRepository;
  @Mock private CampaignService mockCampaignService;
  @Mock private StorageService mockStorageService;
  @Mock private GoogleDriveService mockGoogleDriveService;

  private GoogleDriveUploadConsumer consumer;

  @BeforeEach
  void setUp() {
    final GoogleDriveProperties properties = new GoogleDriveProperties();
    properties.setEnabled(true);
    properties.setRootFolderId(ROOT_FOLDER_ID);
    this.consumer =
        new GoogleDriveUploadConsumer(
            this.mockRedisTemplate,
            this.mockScreenshotRepository,
            this.mockClaimRepository,
            this.mockCampaignService,
            this.mockStorageService,
            this.mockGoogleDriveService,
            properties);
  }

  @Test
  void uploadScreenshotToDrive_uploadsAndUpdatesDb() {
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM_ID)
            .storageKey(STORAGE_KEY)
            .type(ScreenshotType.SCREENSHOT_TYPE_ORDER)
            .verificationStatus(ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
            .isDeleted(false)
            .createdBy(OWNER_ID)
            .updatedBy(OWNER_ID)
            .build();

    final Claim claim =
        Claim.builder().id(CLAIM_ID).campaignId(CAMPAIGN_ID).code(CLAIM_CODE).build();

    final Campaign campaign = Campaign.builder().id(CAMPAIGN_ID).code(CAMPAIGN_CODE).build();

    final GetObjectResponse objectResponse =
        GetObjectResponse.builder().contentType("image/jpeg").build();
    @SuppressWarnings("unchecked")
    final ResponseBytes<GetObjectResponse> responseBytes =
        (ResponseBytes<GetObjectResponse>) ResponseBytes.fromByteArray(objectResponse, FILE_BYTES);

    when(this.mockScreenshotRepository.findById(SCREENSHOT_ID)).thenReturn(Optional.of(screenshot));
    when(this.mockClaimRepository.findById(CLAIM_ID)).thenReturn(Optional.of(claim));
    when(this.mockCampaignService.getById(CAMPAIGN_ID)).thenReturn(campaign);
    when(this.mockStorageService.retrieve(STORAGE_KEY)).thenReturn(responseBytes);
    when(this.mockGoogleDriveService.findOrCreateFolder(ROOT_FOLDER_ID, CAMPAIGN_CODE))
        .thenReturn(CAMPAIGN_FOLDER_ID);
    when(this.mockGoogleDriveService.findOrCreateFolder(CAMPAIGN_FOLDER_ID, CLAIM_CODE))
        .thenReturn(CLAIM_FOLDER_ID);
    when(this.mockGoogleDriveService.uploadFile(
            CLAIM_FOLDER_ID, "screenshot_type_order.jpg", "image/jpeg", FILE_BYTES))
        .thenReturn(DRIVE_URL);

    this.consumer.uploadScreenshotToDrive(SCREENSHOT_ID);

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(DRIVE_URL, screenshotCaptor.getValue().getGoogleDriveUrl());

    final ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
    verify(this.mockClaimRepository).save(claimCaptor.capture());
    assertEquals(CLAIM_FOLDER_ID, claimCaptor.getValue().getGoogleDriveFolderId());
  }

  @Test
  void uploadScreenshotToDrive_skipsDeletedScreenshot() {
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM_ID)
            .storageKey(STORAGE_KEY)
            .type(ScreenshotType.SCREENSHOT_TYPE_ORDER)
            .verificationStatus(ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
            .isDeleted(true)
            .createdBy(OWNER_ID)
            .updatedBy(OWNER_ID)
            .build();

    when(this.mockScreenshotRepository.findById(SCREENSHOT_ID)).thenReturn(Optional.of(screenshot));

    this.consumer.uploadScreenshotToDrive(SCREENSHOT_ID);

    verifyNoInteractions(this.mockGoogleDriveService);
  }

  @Test
  void uploadScreenshotToDrive_reusesCachedClaimFolder() {
    final ClaimScreenshot screenshot =
        ClaimScreenshot.builder()
            .id(SCREENSHOT_ID)
            .claimId(CLAIM_ID)
            .storageKey(STORAGE_KEY)
            .type(ScreenshotType.SCREENSHOT_TYPE_ORDER)
            .verificationStatus(ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
            .isDeleted(false)
            .createdBy(OWNER_ID)
            .updatedBy(OWNER_ID)
            .build();

    final Claim claim =
        Claim.builder()
            .id(CLAIM_ID)
            .campaignId(CAMPAIGN_ID)
            .code(CLAIM_CODE)
            .googleDriveFolderId(CLAIM_FOLDER_ID)
            .build();

    final GetObjectResponse objectResponse =
        GetObjectResponse.builder().contentType("image/jpeg").build();
    @SuppressWarnings("unchecked")
    final ResponseBytes<GetObjectResponse> responseBytes =
        (ResponseBytes<GetObjectResponse>) ResponseBytes.fromByteArray(objectResponse, FILE_BYTES);

    when(this.mockScreenshotRepository.findById(SCREENSHOT_ID)).thenReturn(Optional.of(screenshot));
    when(this.mockClaimRepository.findById(CLAIM_ID)).thenReturn(Optional.of(claim));
    when(this.mockStorageService.retrieve(STORAGE_KEY)).thenReturn(responseBytes);
    when(this.mockGoogleDriveService.uploadFile(
            CLAIM_FOLDER_ID, "screenshot_type_order.jpg", "image/jpeg", FILE_BYTES))
        .thenReturn(DRIVE_URL);

    this.consumer.uploadScreenshotToDrive(SCREENSHOT_ID);

    final ArgumentCaptor<ClaimScreenshot> screenshotCaptor =
        ArgumentCaptor.forClass(ClaimScreenshot.class);
    verify(this.mockScreenshotRepository).save(screenshotCaptor.capture());
    assertEquals(DRIVE_URL, screenshotCaptor.getValue().getGoogleDriveUrl());
  }
}
