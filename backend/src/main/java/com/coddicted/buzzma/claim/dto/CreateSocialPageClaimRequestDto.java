package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateSocialPageClaimRequestDto {

  @NotNull private UUID campaignId;

  @NotNull private UUID dealId;

  private String accountName;

  @NotNull private CampaignStepType stepType;

  @NotNull private MultipartFile screenshot;
}
