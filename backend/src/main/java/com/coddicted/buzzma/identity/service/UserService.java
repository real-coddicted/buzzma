package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.api.UsersRequestDto;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import java.util.List;
import java.util.UUID;

public interface UserService {

  List<UsersResponseDto> list(int limit, int offset);

  UsersResponseDto getById(UUID id);

  UsersResponseDto create(UsersRequestDto request);

  UsersResponseDto update(UUID id, UsersRequestDto request);

  void delete(UUID id);
}
