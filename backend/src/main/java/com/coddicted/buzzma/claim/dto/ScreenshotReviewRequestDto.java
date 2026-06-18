package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class ScreenshotReviewRequestDto {

  @NotNull private UUID screenshotId;

  @NotNull private UUID claimId;

  @NotNull private ScreenshotVerificationStatus action;
}
