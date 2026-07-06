package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.persistence.UserBankingDetailRepository;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserBankingDetailServiceImplTest {

  @Mock private UserBankingDetailRepository mockBankingDetailRepository;
  private UserBankingDetailServiceImpl userBankingDetailService;

  @BeforeEach
  void setUp() {
    this.userBankingDetailService =
        new UserBankingDetailServiceImpl(this.mockBankingDetailRepository);
  }

  @Test
  void testCreate() {
    this.userBankingDetailService.create(BANKING_DETAIL_1, REQUESTER_ID);

    final ArgumentCaptor<UserBankingDetail> captor =
        ArgumentCaptor.forClass(UserBankingDetail.class);
    verify(this.mockBankingDetailRepository).save(captor.capture());
    final UserBankingDetail saved = captor.getValue();
    assertEquals(BANKING_DETAIL_1.getBankDetails(), saved.getBankDetails());
    assertEquals(REQUESTER_ID, saved.getCreatedBy());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testGetByUserIdWhenFound() {
    doReturn(Optional.of(BANKING_DETAIL_1))
        .when(this.mockBankingDetailRepository)
        .findByUserIdAndIsDeletedFalse(USER_ID);

    final UserBankingDetail result = this.userBankingDetailService.getByUserId(USER_ID);

    assertEquals(BANKING_DETAIL_1, result);
  }

  @Test
  void testGetByUserIdWhenNotFound() {
    doReturn(Optional.empty())
        .when(this.mockBankingDetailRepository)
        .findByUserIdAndIsDeletedFalse(USER_ID);

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> this.userBankingDetailService.getByUserId(USER_ID));
    assertEquals("Banking detail not found for user: " + USER_ID, ex.getMessage());
  }
}
