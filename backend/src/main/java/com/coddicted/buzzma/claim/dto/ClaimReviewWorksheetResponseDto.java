package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewWorksheetResponseDto {

  private UUID id;
  private String originalFilename;
  private int rowCount;
  private String storageKey;
  private WorksheetRowStatus status;
  private Instant createdAt;
}
