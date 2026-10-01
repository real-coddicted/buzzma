package com.coddicted.buzzma.storage.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.storage.config.GoogleDriveProperties;
import com.coddicted.buzzma.storage.service.GoogleDriveService;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class GoogleDriveUploadConsumerTest {

  private static final UUID SCREENSHOT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CLAIM_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final String STORAGE_KEY = "claims/abc123.jpg";
  private static final String CAMPAIGN_CODE = "CAM001";
  private static final String CLAIM_CODE = "CLM001";
  private static final String ROOT_FOLDER_ID = "root-folder-123";
  private static final String CAMPAIGN_FOLDER_ID = "campaign-folder-456";
  private static final String CLAIM_FOLDER_ID = "claim-folder-789";
  private static final String DRIVE_URL = "https://drive.google.com/file/d/abc123/view";
  private static final byte[] FILE_BYTES = {1, 2, 3, 4, 5};

  @Mock private StringRedisTemplate mockRedisTemplate;
  @Mock private ClaimService mockClaimService;
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
            new ObjectMapper(),
            this.mockClaimService,
            this.mockStorageService,
            this.mockGoogleDriveService,
            properties);
  }

  @Test
  void uploadScreenshotToDrive_uploadsAndUpdatesDb() {
    final GoogleDriveUploadMessage message =
        new GoogleDriveUploadMessage(
            SCREENSHOT_ID,
            CLAIM_ID,
            CAMPAIGN_CODE,
            CLAIM_CODE,
            STORAGE_KEY,
            "SCREENSHOT_TYPE_ORDER");

    final GetObjectResponse objectResponse =
        GetObjectResponse.builder().contentType("image/jpeg").build();
    @SuppressWarnings("unchecked")
    final ResponseBytes<GetObjectResponse> responseBytes =
        (ResponseBytes<GetObjectResponse>) ResponseBytes.fromByteArray(objectResponse, FILE_BYTES);

    when(this.mockStorageService.retrieve(STORAGE_KEY)).thenReturn(responseBytes);
    when(this.mockGoogleDriveService.findOrCreateFolder(ROOT_FOLDER_ID, CAMPAIGN_CODE))
        .thenReturn(CAMPAIGN_FOLDER_ID);
    when(this.mockGoogleDriveService.findOrCreateFolder(CAMPAIGN_FOLDER_ID, CLAIM_CODE))
        .thenReturn(CLAIM_FOLDER_ID);
    when(this.mockGoogleDriveService.uploadFile(
            CLAIM_FOLDER_ID, "screenshot_type_order.jpg", "image/jpeg", FILE_BYTES))
        .thenReturn(DRIVE_URL);

    this.consumer.uploadScreenshotToDrive(message);

    verify(this.mockClaimService).updateScreenshotGoogleDriveUrl(SCREENSHOT_ID, DRIVE_URL);
  }

  @Test
  void uploadScreenshotToDrive_cachesCampaignFolder() {
    final GoogleDriveUploadMessage message1 =
        new GoogleDriveUploadMessage(
            SCREENSHOT_ID,
            CLAIM_ID,
            CAMPAIGN_CODE,
            CLAIM_CODE,
            STORAGE_KEY,
            "SCREENSHOT_TYPE_ORDER");

    final UUID screenshot2Id = UUID.fromString("55555555-5555-5555-5555-555555555555");
    final GoogleDriveUploadMessage message2 =
        new GoogleDriveUploadMessage(
            screenshot2Id,
            CLAIM_ID,
            CAMPAIGN_CODE,
            "CLM002",
            "claims/def456.jpg",
            "SCREENSHOT_TYPE_REVIEW");

    final GetObjectResponse objectResponse =
        GetObjectResponse.builder().contentType("image/jpeg").build();
    @SuppressWarnings("unchecked")
    final ResponseBytes<GetObjectResponse> responseBytes =
        (ResponseBytes<GetObjectResponse>) ResponseBytes.fromByteArray(objectResponse, FILE_BYTES);

    when(this.mockStorageService.retrieve(STORAGE_KEY)).thenReturn(responseBytes);
    when(this.mockStorageService.retrieve("claims/def456.jpg")).thenReturn(responseBytes);
    when(this.mockGoogleDriveService.findOrCreateFolder(ROOT_FOLDER_ID, CAMPAIGN_CODE))
        .thenReturn(CAMPAIGN_FOLDER_ID);
    when(this.mockGoogleDriveService.findOrCreateFolder(CAMPAIGN_FOLDER_ID, CLAIM_CODE))
        .thenReturn(CLAIM_FOLDER_ID);
    when(this.mockGoogleDriveService.findOrCreateFolder(CAMPAIGN_FOLDER_ID, "CLM002"))
        .thenReturn("claim-folder-999");
    when(this.mockGoogleDriveService.uploadFile(
            CLAIM_FOLDER_ID, "screenshot_type_order.jpg", "image/jpeg", FILE_BYTES))
        .thenReturn(DRIVE_URL);
    when(this.mockGoogleDriveService.uploadFile(
            "claim-folder-999", "screenshot_type_review.jpg", "image/jpeg", FILE_BYTES))
        .thenReturn("https://drive.google.com/file/d/def456/view");

    this.consumer.uploadScreenshotToDrive(message1);
    this.consumer.uploadScreenshotToDrive(message2);

    verify(this.mockGoogleDriveService, Mockito.times(1))
        .findOrCreateFolder(ROOT_FOLDER_ID, CAMPAIGN_CODE);
  }
}
