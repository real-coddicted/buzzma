package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.persistence.UserBankingDetailRepository;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

  @Test
  void testGetByUserIdsReturnsDetailsKeyedByUserId() {
    final UserBankingDetail detail = BANKING_DETAIL_1.toBuilder().userId(USER_ID).build();
    doReturn(List.of(detail))
        .when(this.mockBankingDetailRepository)
        .findByUserIdInAndIsDeletedFalse(Set.of(USER_ID, REQUESTER_ID));

    final Map<UUID, UserBankingDetail> result =
        this.userBankingDetailService.getByUserIds(Set.of(USER_ID, REQUESTER_ID));

    assertEquals(Map.of(USER_ID, detail), result);
  }

  @Test
  void testGetByUserIdsWhenUserIdsEmptyReturnsEmptyMapWithoutQuery() {
    final Map<UUID, UserBankingDetail> result =
        this.userBankingDetailService.getByUserIds(Set.of());

    assertTrue(result.isEmpty());
    verifyNoInteractions(this.mockBankingDetailRepository);
  }
}
