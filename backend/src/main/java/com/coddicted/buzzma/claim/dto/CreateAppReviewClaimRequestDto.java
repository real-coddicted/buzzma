package com.coddicted.buzzma.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateAppReviewClaimRequestDto {

  @NotNull private UUID campaignId;

  @NotNull private UUID dealId;

  @NotBlank private String productName;

  @NotBlank private String accountName;

  @NotNull private MultipartFile screenshot;
}
