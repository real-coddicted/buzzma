package com.coddicted.buzzma.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.UUID;

@Data
public class OrderRequestDto {

    @NotNull
    private UUID campaignId;

    @NotNull
    private UUID dealId;

    @NotBlank
    private String orderId;

    @NotNull
    private BigInteger amount;

    @NotBlank
    private String productName;

    @NotBlank
    private String sellerName;

    @NotBlank
    private int orderDate;

    @NotBlank
    private String accountName;

    @NotNull
    final MultipartFile screenshot;
}
