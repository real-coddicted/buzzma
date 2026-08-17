package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.dto.UpdateProfileRequestDto;
import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.dto.UserBatchRequestDto;
import com.coddicted.buzzma.identity.dto.UserBriefDto;
import com.coddicted.buzzma.identity.dto.UserSummaryDto;
import com.coddicted.buzzma.identity.dto.VerifyEmailOtpRequestDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.mapper.UserBankingDetailMapper;
import com.coddicted.buzzma.identity.mapper.UserMapper;
import com.coddicted.buzzma.identity.service.EmailVerificationService;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.security.CurrentUser;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UsersController {

  private final UserService userService;
  private final UserMapper userMapper;
  private final UserBankingDetailService userBankingDetailService;
  private final UserBankingDetailMapper userBankingDetailMapper;
  private final EmailVerificationService emailVerificationService;

  public UsersController(
      final UserService userService,
      final UserMapper userMapper,
      final UserBankingDetailService userBankingDetailService,
      final UserBankingDetailMapper userBankingDetailMapper,
      final EmailVerificationService emailVerificationService) {
    this.userService = userService;
    this.userMapper = userMapper;
    this.userBankingDetailService = userBankingDetailService;
    this.userBankingDetailMapper = userBankingDetailMapper;
    this.emailVerificationService = emailVerificationService;
  }

  @GetMapping("/me")
  public UserSummaryDto me(@CurrentUserId final UUID requesterId) {
    final BuzzmaUser user = this.userService.getById(requesterId);
    return this.userMapper.toUserSummaryDto(user);
  }

  @PostMapping("/me")
  public UserSummaryDto updateProfile(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final UpdateProfileRequestDto request) {
    final BuzzmaUser user = this.userService.updateProfile(request.getEmail(), requesterId);
    return this.userMapper.toUserSummaryDto(user);
  }

  @PostMapping("/me/email/otp/send")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void sendEmailOtp(@CurrentUser final BuzzmaUser currentUser) {
    this.emailVerificationService.sendOtp(currentUser.getId());
  }

  @PostMapping("/me/email/otp/verify")
  public UserSummaryDto verifyEmailOtp(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final VerifyEmailOtpRequestDto request) {
    this.emailVerificationService.verifyOtp(requesterId, request.getCode());
    final BuzzmaUser user = this.userService.getById(requesterId);
    return this.userMapper.toUserSummaryDto(user);
  }

  @GetMapping("/search")
  public UserSummaryDto searchByMobile(@RequestParam @NotBlank final String mobile) {
    final BuzzmaUser user = this.userService.getByMobile(mobile);
    return this.userMapper.toUserSummaryDto(user);
  }

  /** Bulk lookup for display purposes, restricted to users connected to the caller. */
  @PostMapping("/batch")
  public List<UserBriefDto> getByIds(
      @RequestBody final UserBatchRequestDto request, @CurrentUserId final UUID requesterId) {
    final Set<UUID> requestedIds = request.getIds() == null ? Set.of() : request.getIds();
    if (requestedIds.isEmpty()) {
      return List.of();
    }

    final List<BuzzmaUser> connectedUsers =
        this.userService.getConnectedByIds(new ArrayList<>(requestedIds), requesterId);
    final Set<UUID> connectedIds =
        connectedUsers.stream().map(BuzzmaUser::getId).collect(Collectors.toSet());
    final Map<UUID, UserBankingDetail> bankingByUserId =
        this.userBankingDetailService.getByUserIds(connectedIds);
    return connectedUsers.stream()
        .map(
            user ->
                UserBriefDto.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .role(user.getRole())
                    .upiId(upiIdOf(bankingByUserId.get(user.getId())))
                    .build())
        .toList();
  }

  private String upiIdOf(final UserBankingDetail bankingDetail) {
    if (bankingDetail == null || bankingDetail.getUpiDetails() == null) {
      return null;
    }
    return bankingDetail.getUpiDetails().getUpiId();
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "(("
          + UserRole.Expr.AGENCY
          + UserRole.Expr.OR
          + UserRole.Expr.MEDIATOR
          + UserRole.Expr.OR
          + UserRole.Expr.BRAND
          + ")"
          + UserRole.Expr.AND
          + "@parentshipGuard.isParentOf(#id))"
          + UserRole.Expr.OR
          + UserRole.Expr.ADMIN)
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
