package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.api.InvitesRequestDto;
import com.coddicted.buzzma.identity.api.InvitesResponseDto;
import java.util.List;
import java.util.UUID;

public interface InviteService {

  List<InvitesResponseDto> list(int limit, int offset);

  InvitesResponseDto getById(UUID id);

  InvitesResponseDto create(InvitesRequestDto request);

  InvitesResponseDto update(UUID id, InvitesRequestDto request);

  void delete(UUID id);
}
