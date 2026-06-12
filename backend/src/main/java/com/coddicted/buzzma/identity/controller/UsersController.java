package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.dto.UserSummaryDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.mapper.UserMapper;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UsersController {

  private final UserService userService;
  private final UserMapper userMapper;

  public UsersController(final UserService userService, final UserMapper userMapper) {
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @GetMapping("/me")
  public UserSummaryDto me(@CurrentUserId final UUID requesterId) {
    final BuzzmaUser user = this.userService.getById(requesterId);
    return this.userMapper.toUserSummaryDto(user);
  }
}
