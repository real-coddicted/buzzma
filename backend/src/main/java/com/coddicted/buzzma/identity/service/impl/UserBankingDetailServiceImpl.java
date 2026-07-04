package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.entity.BankDetails;
import com.coddicted.buzzma.identity.entity.UpiDetails;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.persistence.UserBankingDetailRepository;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBankingDetailServiceImpl extends BaseCrudService
    implements UserBankingDetailService {

  private final UserBankingDetailRepository userBankingDetailRepository;

  public UserBankingDetailServiceImpl(
      final UserBankingDetailRepository userBankingDetailRepository) {
    this.userBankingDetailRepository = userBankingDetailRepository;
  }

  @Override
  public UserBankingDetail create(
      final UserBankingDetail userBankingDetail, final UUID requesterId) {
    return userBankingDetailRepository.save(
        userBankingDetail.toBuilder().createdBy(requesterId).updatedBy(requesterId).build());
  }

  @Override
  @Transactional(readOnly = true)
  public UserBankingDetail getByUserId(final UUID userId) {
    return this.userBankingDetailRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new NotFoundException("Banking detail not found for user: " + userId));
  }

  @Override
  @Transactional
  public UserBankingDetail update(
      final UUID userId, final UserBankingDetailDto dto, final UUID requesterId) {
    final UserBankingDetail existing = getByUserId(userId);
    return userBankingDetailRepository.save(
        existing.toBuilder()
            .bankDetails(toBankDetails(dto))
            .upiDetails(toUpiDetails(dto))
            .updatedBy(requesterId)
            .build());
  }

  @Override
  @Transactional
  public UserBankingDetail upsert(
      final UUID userId, final UserBankingDetailDto dto, final UUID requesterId) {
    final Optional<UserBankingDetail> existing =
        userBankingDetailRepository.findByUserIdAndIsDeletedFalse(userId);
    if (existing.isPresent()) {
      return update(userId, dto, requesterId);
    }
    return create(
        UserBankingDetail.builder()
            .userId(userId)
            .bankDetails(toBankDetails(dto))
            .upiDetails(toUpiDetails(dto))
            .build(),
        requesterId);
  }

  private BankDetails toBankDetails(final UserBankingDetailDto dto) {
    return BankDetails.builder()
        .accountNumber(dto.getBankAccountNumber())
        .bankIfscCode(dto.getBankIfscCode())
        .bankName(dto.getBankName())
        .accountHolderName(dto.getBankAccountHolderName())
        .build();
  }

  private UpiDetails toUpiDetails(final UserBankingDetailDto dto) {
    return UpiDetails.builder()
        .upiId(dto.getUpiId())
        .mobileNumber(dto.getUpiMobileNumber())
        .build();
  }
}
