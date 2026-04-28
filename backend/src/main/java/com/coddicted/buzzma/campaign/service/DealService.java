package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.api.DealRequestDto;
import com.coddicted.buzzma.campaign.api.DealResponseDto;

import java.util.List;
import java.util.UUID;

public interface DealService {

  List<DealResponseDto> list(int limit, int offset);

  DealResponseDto getById(UUID id);

  DealResponseDto create(DealRequestDto request);

  DealResponseDto update(UUID id, DealRequestDto request);

  void delete(UUID id);
}
