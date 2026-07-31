package com.coddicted.buzzma.support.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.persistence.TicketCategoryRepository;
import com.coddicted.buzzma.support.persistence.TicketSubCategoryRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketCategoryServiceImplTest {

  private static final TicketCategory TECHNICAL_CATEGORY =
      TicketCategory.builder().code("TICKET_CATEGORY_TECHNICAL").build();
  private static final TicketCategory CLAIM_CATEGORY =
      TicketCategory.builder().code("TICKET_CATEGORY_CLAIM").build();
  private static final TicketCategory CAMPAIGN_CATEGORY =
      TicketCategory.builder().code("TICKET_CATEGORY_CAMPAIGN").build();

  private static final List<TicketCategory> ALL_CATEGORIES =
      List.of(TECHNICAL_CATEGORY, CLAIM_CATEGORY, CAMPAIGN_CATEGORY);

  @Mock private TicketCategoryRepository mockCategoryRepository;
  @Mock private TicketSubCategoryRepository mockSubCategoryRepository;
  private TicketCategoryServiceImpl ticketCategoryService;

  @BeforeEach
  void setUp() {
    this.ticketCategoryService =
        new TicketCategoryServiceImpl(this.mockCategoryRepository, this.mockSubCategoryRepository);
  }

  @Test
  void listActiveCategories_buyer_seesTechnicalAndClaim() {
    when(this.mockCategoryRepository.findAllByIsDeletedFalse()).thenReturn(ALL_CATEGORIES);

    final List<TicketCategory> result =
        this.ticketCategoryService.listActiveCategories(UserRole.ROLE_BUYER);

    assertEquals(List.of(TECHNICAL_CATEGORY, CLAIM_CATEGORY), result);
  }

  @Test
  void listActiveCategories_mediator_seesTechnicalAndCampaign() {
    when(this.mockCategoryRepository.findAllByIsDeletedFalse()).thenReturn(ALL_CATEGORIES);

    final List<TicketCategory> result =
        this.ticketCategoryService.listActiveCategories(UserRole.ROLE_MEDIATOR);

    assertEquals(List.of(TECHNICAL_CATEGORY, CAMPAIGN_CATEGORY), result);
  }

  @Test
  void listActiveCategories_agency_seesTechnicalOnly() {
    when(this.mockCategoryRepository.findAllByIsDeletedFalse()).thenReturn(ALL_CATEGORIES);

    final List<TicketCategory> result =
        this.ticketCategoryService.listActiveCategories(UserRole.ROLE_AGENCY);

    assertEquals(List.of(TECHNICAL_CATEGORY), result);
  }

  @Test
  void listActiveCategories_brand_seesTechnicalOnly() {
    when(this.mockCategoryRepository.findAllByIsDeletedFalse()).thenReturn(ALL_CATEGORIES);

    final List<TicketCategory> result =
        this.ticketCategoryService.listActiveCategories(UserRole.ROLE_BRAND);

    assertEquals(List.of(TECHNICAL_CATEGORY), result);
  }

  @Test
  void listActiveCategories_admin_seesAllCategories() {
    when(this.mockCategoryRepository.findAllByIsDeletedFalse()).thenReturn(ALL_CATEGORIES);

    final List<TicketCategory> result =
        this.ticketCategoryService.listActiveCategories(UserRole.ROLE_ADMIN);

    assertEquals(ALL_CATEGORIES, result);
  }
}
