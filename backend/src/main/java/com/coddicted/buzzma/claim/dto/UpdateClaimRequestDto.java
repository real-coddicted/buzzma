package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ScreenshotType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateClaimRequestDto {

  @NotNull private UUID screenshotId;

  @NotNull private ScreenshotType screenshotType;

  @NotNull private MultipartFile screenshot;
}
