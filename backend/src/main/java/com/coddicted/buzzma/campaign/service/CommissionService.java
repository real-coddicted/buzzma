package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.dto.CommissionResponseDto;
import java.util.UUID;

public interface CommissionService {
  CommissionResponseDto getCommissionCharged(UUID campaignId, UUID chargedById);
}
