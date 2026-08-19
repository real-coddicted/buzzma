package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DealServiceImplTest {

  @Mock private DealRepository mockDealRepository;
  @Mock private CodeGenerationService mockCodeGenerationService;
  private DealServiceImpl dealService;

  @BeforeEach
  void setUp() {
    this.dealService = new DealServiceImpl(this.mockDealRepository, this.mockCodeGenerationService);
  }

  @Test
  void testGetByIdWhenFound() {
    when(this.mockDealRepository.findById(DEAL_ID)).thenReturn(Optional.of(DEAL_1));

    final Deal result = this.dealService.getById(DEAL_ID);

    assertEquals(DEAL_1, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    when(this.mockDealRepository.findById(DEAL_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.dealService.getById(DEAL_ID));
    assertEquals("Deal not found: " + DEAL_ID, ex.getMessage());
  }

  @Test
  void testCreate() {
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.DEAL))
        .thenReturn(DEAL_CODE);
    final Deal saved = DEAL_1.toBuilder().code(DEAL_CODE).build();
    when(this.mockDealRepository.save(any(Deal.class))).thenReturn(saved);

    final Deal result = this.dealService.create(DEAL_1);

    assertEquals(saved, result);
  }

  @Test
  void testGetActiveDeals() {
    final Page<Deal> dealPage = new PageImpl<>(List.of(DEAL_1));
    when(this.mockDealRepository.findActiveDeals(
            eq(List.of(OWNER_ID)), anyInt(), eq(PageRequest.of(0, 10))))
        .thenReturn(dealPage);

    final Page<Deal> result =
        this.dealService.getActiveDeals(List.of(OWNER_ID), REQUESTER_ID, 0, 10);

    assertEquals(dealPage, result);
  }

  @Test
  void testGetActiveDealsPassesAsianTodayDate() {
    when(this.mockDealRepository.findActiveDeals(
            eq(List.of(OWNER_ID)), anyInt(), eq(PageRequest.of(0, 10))))
        .thenReturn(new PageImpl<>(List.of(DEAL_1)));

    this.dealService.getActiveDeals(List.of(OWNER_ID), REQUESTER_ID, 0, 10);

    final ArgumentCaptor<Integer> todayCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(this.mockDealRepository)
        .findActiveDeals(eq(List.of(OWNER_ID)), todayCaptor.capture(), eq(PageRequest.of(0, 10)));
    assertEquals(DateTimeUtils.getAsianTodayDate(), todayCaptor.getValue());
  }
}
