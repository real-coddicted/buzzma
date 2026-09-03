package com.coddicted.buzzma.exchange.service.impl;

import static com.coddicted.buzzma.exchange.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.exchange.entity.AgencyExchangeProduct;
import com.coddicted.buzzma.exchange.persistence.AgencyExchangeProductRepository;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeProductServiceImplTest {

  @Mock private AgencyExchangeProductRepository mockRepository;
  private ExchangeProductServiceImpl service;

  @BeforeEach
  void setUp() {
    this.service = new ExchangeProductServiceImpl(this.mockRepository);
  }

  @Test
  void testList() {
    when(this.mockRepository.findByAgencyIdAndIsDeletedFalseOrderByNameAsc(AGENCY_ID))
        .thenReturn(List.of(PRODUCT_1, PRODUCT_2));

    final List<AgencyExchangeProduct> result = this.service.list(AGENCY_ID);

    assertEquals(2, result.size());
    assertEquals(PRODUCT_1, result.get(0));
    assertEquals(PRODUCT_2, result.get(1));
  }

  @Test
  void testCreate() {
    when(this.mockRepository.existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(
            AGENCY_ID, "Toor Dal 1kg"))
        .thenReturn(false);

    this.service.create(NEW_PRODUCT, AGENCY_ID);

    final ArgumentCaptor<AgencyExchangeProduct> captor =
        ArgumentCaptor.forClass(AgencyExchangeProduct.class);
    verify(this.mockRepository).save(captor.capture());
    final AgencyExchangeProduct saved = captor.getValue();
    assertEquals("Toor Dal 1kg", saved.getName());
    assertEquals(AGENCY_ID, saved.getAgencyId());
    assertEquals(AGENCY_ID, saved.getCreatedBy());
    assertEquals(AGENCY_ID, saved.getUpdatedBy());
    assertFalse(saved.getIsDeleted());
  }

  @Test
  void testCreateTrimsName() {
    when(this.mockRepository.existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(
            AGENCY_ID, "Toor Dal 1kg"))
        .thenReturn(false);

    this.service.create(NEW_PRODUCT.toBuilder().name("  Toor Dal 1kg  ").build(), AGENCY_ID);

    final ArgumentCaptor<AgencyExchangeProduct> captor =
        ArgumentCaptor.forClass(AgencyExchangeProduct.class);
    verify(this.mockRepository).save(captor.capture());
    assertEquals("Toor Dal 1kg", captor.getValue().getName());
  }

  @Test
  void testCreateRejectsDuplicateName() {
    when(this.mockRepository.existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(
            AGENCY_ID, "Toor Dal 1kg"))
        .thenReturn(true);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> this.service.create(NEW_PRODUCT, AGENCY_ID));
    assertEquals("Exchange product already exists: Toor Dal 1kg", ex.getMessage());
    verify(this.mockRepository, never()).save(any());
  }

  @Test
  void testUpdate() {
    when(this.mockRepository.findByIdAndAgencyIdAndIsDeletedFalse(PRODUCT_ID, AGENCY_ID))
        .thenReturn(Optional.of(PRODUCT_1));
    when(this.mockRepository.existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(
            AGENCY_ID, "Sona Masoori Rice 5kg"))
        .thenReturn(false);

    this.service.update(PRODUCT_1.toBuilder().name("Sona Masoori Rice 5kg").build(), AGENCY_ID);

    final ArgumentCaptor<AgencyExchangeProduct> captor =
        ArgumentCaptor.forClass(AgencyExchangeProduct.class);
    verify(this.mockRepository).save(captor.capture());
    final AgencyExchangeProduct saved = captor.getValue();
    assertEquals(PRODUCT_ID, saved.getId());
    assertEquals("Sona Masoori Rice 5kg", saved.getName());
    assertEquals(AGENCY_ID, saved.getUpdatedBy());
    assertEquals(AGENCY_ID, saved.getAgencyId());
  }

  @Test
  void testUpdateThrowsWhenNotOwnedByAgency() {
    when(this.mockRepository.findByIdAndAgencyIdAndIsDeletedFalse(MISSING_ID, AGENCY_ID))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                this.service.update(
                    PRODUCT_1.toBuilder().id(MISSING_ID).name("Anything").build(), AGENCY_ID));
    assertEquals("Exchange product not found: " + MISSING_ID, ex.getMessage());
    verify(this.mockRepository, never()).save(any());
  }

  @Test
  void testUpdateRejectsDuplicateName() {
    when(this.mockRepository.findByIdAndAgencyIdAndIsDeletedFalse(PRODUCT_ID, AGENCY_ID))
        .thenReturn(Optional.of(PRODUCT_1));
    when(this.mockRepository.existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(
            AGENCY_ID, "Sunflower Oil 1L"))
        .thenReturn(true);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.service.update(
                    PRODUCT_1.toBuilder().name("Sunflower Oil 1L").build(), AGENCY_ID));
    assertEquals("Exchange product already exists: Sunflower Oil 1L", ex.getMessage());
    verify(this.mockRepository, never()).save(any());
  }

  @Test
  void testUpdateSkipsDuplicateCheckWhenNameUnchanged() {
    when(this.mockRepository.findByIdAndAgencyIdAndIsDeletedFalse(PRODUCT_ID, AGENCY_ID))
        .thenReturn(Optional.of(PRODUCT_1));

    this.service.update(PRODUCT_1.toBuilder().name("basmati rice 5kg").build(), AGENCY_ID);

    verify(this.mockRepository, never())
        .existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(AGENCY_ID, "basmati rice 5kg");
    final ArgumentCaptor<AgencyExchangeProduct> captor =
        ArgumentCaptor.forClass(AgencyExchangeProduct.class);
    verify(this.mockRepository).save(captor.capture());
    assertEquals("basmati rice 5kg", captor.getValue().getName());
  }

  @Test
  void testDelete() {
    when(this.mockRepository.findByIdAndAgencyIdAndIsDeletedFalse(PRODUCT_ID, AGENCY_ID))
        .thenReturn(Optional.of(PRODUCT_1));

    this.service.delete(PRODUCT_ID, AGENCY_ID);

    final ArgumentCaptor<AgencyExchangeProduct> captor =
        ArgumentCaptor.forClass(AgencyExchangeProduct.class);
    verify(this.mockRepository).save(captor.capture());
    final AgencyExchangeProduct saved = captor.getValue();
    assertEquals(PRODUCT_ID, saved.getId());
    assertTrue(saved.getIsDeleted());
    assertEquals(AGENCY_ID, saved.getUpdatedBy());
  }

  @Test
  void testDeleteThrowsWhenNotOwnedByAgency() {
    when(this.mockRepository.findByIdAndAgencyIdAndIsDeletedFalse(MISSING_ID, AGENCY_ID))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.service.delete(MISSING_ID, AGENCY_ID));
    assertEquals("Exchange product not found: " + MISSING_ID, ex.getMessage());
    verify(this.mockRepository, never()).save(any());
  }
}
