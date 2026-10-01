package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ScreenshotType;
import java.util.UUID;

public record PendingR2UploadView(
    UUID screenshotId,
    UUID claimId,
    String claimCode,
    UUID campaignId,
    String storageKey,
    ScreenshotType type) {}
