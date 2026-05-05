package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.persistence.UserBankingDetailRepository;
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
    assertEquals(BANKING_DETAIL_1.getAccountNumber(), saved.getAccountNumber());
    assertEquals(BANKING_DETAIL_1.getBankName(), saved.getBankName());
    assertEquals(BANKING_DETAIL_1.getBankIfscCode(), saved.getBankIfscCode());
    assertEquals(BANKING_DETAIL_1.getAccountHolderName(), saved.getAccountHolderName());
    assertEquals(REQUESTER_ID, saved.getCreatedBy());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }
}
