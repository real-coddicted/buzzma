package com.coddicted.buzzma.storage.event;

import java.util.UUID;

public record GoogleDriveUploadMessage(
    UUID screenshotId,
    UUID claimId,
    String campaignCode,
    String claimCode,
    String storageKey,
    String screenshotType) {}
