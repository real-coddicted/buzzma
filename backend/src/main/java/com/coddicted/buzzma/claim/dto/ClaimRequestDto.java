package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.enums.Platform;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ClaimRequestDto {

  @NotNull private UUID campaignId;

  @NotNull private UUID dealId;

  @NotNull private Platform platform;

  @NotBlank private String orderId;

  @NotNull private BigInteger amount;

  @NotBlank private String productName;

  @Nullable private String sellerName;

  @Nullable private String exchangeProduct;

  private int orderDate;

  @NotBlank private String accountName;

  @NotNull final MultipartFile screenshot;

  /** Absent when client-side sync extraction failed and the buyer entered details manually. */
  @Nullable private Map<String, ScoredValue> extractedDetails;

  private Integer overallScore;
}
