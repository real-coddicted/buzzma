package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.campaign.entity.Commission;
import com.coddicted.buzzma.campaign.persistence.CommissionRepository;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommissionServiceImplTest {

  @Mock private CommissionRepository mockCommissionRepository;
  private CommissionServiceImpl commissionService;

  @BeforeEach
  void setUp() {
    this.commissionService = new CommissionServiceImpl(this.mockCommissionRepository);
  }

  @Test
  void testGetCommissionChargedWhenFound() {
    doReturn(Optional.of(COMMISSION_1))
        .when(this.mockCommissionRepository)
        .findByCampaignIdAndChargedByIdAndIsDeletedFalse(CAMPAIGN_ID_1, OWNER_ID);

    final Commission result =
        this.commissionService.getCommissionCharged(CAMPAIGN_ID_1, OWNER_ID);

    assertEquals(COMMISSION_1, result);
  }

  @Test
  void testGetCommissionChargedWhenNotFound() {
    doReturn(Optional.empty())
        .when(this.mockCommissionRepository)
        .findByCampaignIdAndChargedByIdAndIsDeletedFalse(CAMPAIGN_ID_1, OWNER_ID);

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> this.commissionService.getCommissionCharged(CAMPAIGN_ID_1, OWNER_ID));
    assertEquals("Commission not found for campaign: " + CAMPAIGN_ID_1, ex.getMessage());
  }

  @Test
  void testCreate() {
    this.commissionService.create(COMMISSION_1, REQUESTER_ID);

    final ArgumentCaptor<Commission> captor = ArgumentCaptor.forClass(Commission.class);
    verify(this.mockCommissionRepository).save(captor.capture());
    final Commission saved = captor.getValue();
    assertFalse(saved.isDeleted());
    assertEquals(REQUESTER_ID, saved.getCreatedBy());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
    assertEquals(CAMPAIGN_ID_1, saved.getCampaignId());
  }

  @Test
  void testUpdate() {
    doReturn(Optional.of(COMMISSION_1))
        .when(this.mockCommissionRepository)
        .findById(COMMISSION_ID);

    this.commissionService.update(COMMISSION_2, REQUESTER_ID);

    final ArgumentCaptor<Commission> captor = ArgumentCaptor.forClass(Commission.class);
    verify(this.mockCommissionRepository).save(captor.capture());
    final Commission saved = captor.getValue();
    assertEquals(COMMISSION_ID, saved.getId());
    assertEquals(COMMISSION_UPDATED_PAISE, saved.getCommissionPaise());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testUpdateWhenNotFound() {
    doReturn(Optional.empty())
        .when(this.mockCommissionRepository)
        .findById(COMMISSION_ID);

    assertThrows(
        NotFoundException.class,
        () -> this.commissionService.update(COMMISSION_2, REQUESTER_ID));
  }

  @Test
  void testDelete() {
    doReturn(Optional.of(COMMISSION_1))
        .when(this.mockCommissionRepository)
        .findById(COMMISSION_ID);

    this.commissionService.delete(COMMISSION_ID, REQUESTER_ID);

    final ArgumentCaptor<Commission> captor = ArgumentCaptor.forClass(Commission.class);
    verify(this.mockCommissionRepository).save(captor.capture());
    final Commission saved = captor.getValue();
    assertEquals(COMMISSION_ID, saved.getId());
    assertTrue(saved.isDeleted());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testDeleteWhenNotFound() {
    doReturn(Optional.empty())
        .when(this.mockCommissionRepository)
        .findById(COMMISSION_ID);

    assertThrows(
        NotFoundException.class,
        () -> this.commissionService.delete(COMMISSION_ID, REQUESTER_ID));
  }
}
