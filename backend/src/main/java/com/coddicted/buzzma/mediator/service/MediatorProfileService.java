package com.coddicted.buzzma.mediator.service;

import com.coddicted.buzzma.mediator.api.MediatorProfilesRequestDto;
import com.coddicted.buzzma.mediator.api.MediatorProfilesResponseDto;
import java.util.List;
import java.util.UUID;

public interface MediatorProfileService {

  List<MediatorProfilesResponseDto> list(int limit, int offset);

  MediatorProfilesResponseDto getById(UUID id);

  MediatorProfilesResponseDto create(MediatorProfilesRequestDto request);

  MediatorProfilesResponseDto update(UUID id, MediatorProfilesRequestDto request);

  void delete(UUID id);
}
