package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.dto.UserSummaryDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.mapper.UserBankingDetailMapper;
import com.coddicted.buzzma.identity.mapper.UserMapper;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UsersController {

  private final UserService userService;
  private final UserMapper userMapper;
  private final UserBankingDetailService userBankingDetailService;
  private final UserBankingDetailMapper userBankingDetailMapper;

  public UsersController(
      final UserService userService,
      final UserMapper userMapper,
      final UserBankingDetailService userBankingDetailService,
      final UserBankingDetailMapper userBankingDetailMapper) {
    this.userService = userService;
    this.userMapper = userMapper;
    this.userBankingDetailService = userBankingDetailService;
    this.userBankingDetailMapper = userBankingDetailMapper;
  }

  @GetMapping("/me")
  @PreAuthorize("@ownershipGuard.isOwner(#requesterId)")
  public UserSummaryDto me(@CurrentUserId final UUID requesterId) {
    final BuzzmaUser user = this.userService.getById(requesterId);
    return this.userMapper.toUserSummaryDto(user);
  }

  @GetMapping("/search")
  public UserSummaryDto searchByMobile(@RequestParam @NotBlank final String mobile) {
    final BuzzmaUser user = this.userService.getByMobile(mobile);
    return this.userMapper.toUserSummaryDto(user);
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "(hasAnyRole('AGENCY', 'MEDIATOR') and @parentshipGuard.isParentOf(#id)) or hasRole('ADMIN')")
  public UserSummaryDto getById(@PathVariable final UUID id) {
    final BuzzmaUser user = this.userService.getById(id);
    return this.userMapper.toUserSummaryDto(user);
  }

  @GetMapping("/{id}/banking")
  @PreAuthorize("@ownershipGuard.isOwner(#id) or @parentshipGuard.isParentOf(#id)")
  public UserBankingDetailDto getBankingDetail(@PathVariable final UUID id) {
    final UserBankingDetail bankingDetail = this.userBankingDetailService.getByUserId(id);
    return this.userBankingDetailMapper.toDto(bankingDetail);
  }

  @PutMapping("/{id}/banking")
  @PreAuthorize("@ownershipGuard.isOwner(#id)")
  public UserBankingDetailDto upsertBankingDetail(
      @PathVariable final UUID id,
      @RequestBody final UserBankingDetailDto dto,
      @CurrentUserId final UUID requesterId) {
    final UserBankingDetail bankingDetail =
        this.userBankingDetailService.upsert(id, dto, requesterId);
    return this.userBankingDetailMapper.toDto(bankingDetail);
  }
}
