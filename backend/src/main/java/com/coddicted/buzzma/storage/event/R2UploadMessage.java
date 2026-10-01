package com.coddicted.buzzma.storage.event;

import java.util.UUID;

public record R2UploadMessage(
    UUID screenshotId,
    UUID claimId,
    String campaignCode,
    String claimCode,
    String storageKey,
    String screenshotType) {}
