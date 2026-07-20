package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.shared.enums.Platform;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateClaimRequestDto {

  @NotNull private UUID screenshotId;

  @NotNull private ScreenshotType screenshotType;

  @NotNull private MultipartFile screenshot;

  private Platform platform;
  private String orderId;
  private BigInteger amount;
  private String productName;
  private String sellerName;
  private Integer orderDate;
  private String accountName;
  private String reviewUrl;
}
