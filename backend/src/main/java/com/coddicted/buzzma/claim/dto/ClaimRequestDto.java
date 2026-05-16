package com.coddicted.buzzma.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ClaimRequestDto {

  @NotNull private UUID campaignId;

  @NotNull private UUID dealId;

  @NotBlank private String orderId;

  @NotNull private BigInteger amount;

  @NotBlank private String productName;

  @NotBlank private String sellerName;

  @NotBlank private int orderDate;

  @NotBlank private String accountName;

  @NotNull final MultipartFile screenshot;
}
