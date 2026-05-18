package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DealServiceImplTest {

  @Mock private DealRepository mockDealRepository;
  private DealServiceImpl dealService;

  @BeforeEach
  void setUp() {
    this.dealService = new DealServiceImpl(this.mockDealRepository);
  }

  @Test
  void testGetByIdWhenFound() {
    doReturn(Optional.of(DEAL_1)).when(this.mockDealRepository).findById(DEAL_ID);

    final Deal result = this.dealService.getById(DEAL_ID);

    assertEquals(DEAL_1, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    doReturn(Optional.empty()).when(this.mockDealRepository).findById(DEAL_ID);

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.dealService.getById(DEAL_ID));
    assertEquals("Deal not found: " + DEAL_ID, ex.getMessage());
  }

  @Test
  void testCreate() {
    doReturn(DEAL_1).when(this.mockDealRepository).save(DEAL_1);

    final Deal result = this.dealService.create(DEAL_1);

    assertEquals(DEAL_1, result);
  }

  @Test
  void testGetUnclaimedDeals() {
    final Page<Deal> dealPage = new PageImpl<>(List.of(DEAL_1));
    doReturn(dealPage)
        .when(this.mockDealRepository)
        .findUnclaimedDeals(OWNER_ID, REQUESTER_ID, PageRequest.of(0, 10));

    final Page<Deal> result =
        this.dealService.getUnclaimedDeals(OWNER_ID, REQUESTER_ID, 0, 10);

    assertEquals(dealPage, result);
  }
}
