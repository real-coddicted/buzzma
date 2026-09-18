package com.coddicted.buzzma.claim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkClaimsReadyForAccountingResponseDto {

  private int updatedCount;
}
