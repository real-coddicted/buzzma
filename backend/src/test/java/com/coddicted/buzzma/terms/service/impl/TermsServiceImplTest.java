package com.coddicted.buzzma.terms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.terms.dto.TermsAcceptanceStatusDto;
import com.coddicted.buzzma.terms.dto.TermsDto;
import com.coddicted.buzzma.terms.entity.UserTermsAcceptance;
import com.coddicted.buzzma.terms.persistence.UserTermsAcceptanceRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TermsServiceImplTest {

  private static final UUID USER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

  @Mock private UserTermsAcceptanceRepository mockUserTermsAcceptanceRepository;
  private TermsServiceImpl termsService;

  @BeforeEach
  void setUp() {
    this.termsService = new TermsServiceImpl(this.mockUserTermsAcceptanceRepository);
  }

  @Test
  void testGetCurrentReturnsContentAndVersion() {
    final TermsDto result = this.termsService.getCurrent();

    assertEquals("v1.0", result.getVersion());
    assertTrue(result.getContent().contains("Buzzmah Agency Terms"));
  }

  @Test
  void testGetAcceptanceStatusWhenNoAcceptanceRowReturnsMustReaccept() {
    when(this.mockUserTermsAcceptanceRepository.findTopByUserIdOrderByAcceptedAtDesc(USER_ID))
        .thenReturn(Optional.empty());

    final TermsAcceptanceStatusDto result = this.termsService.getAcceptanceStatus(USER_ID);

    assertTrue(result.isMustReaccept());
  }

  @Test
  void testGetAcceptanceStatusWhenStaleVersionReturnsMustReaccept() {
    when(this.mockUserTermsAcceptanceRepository.findTopByUserIdOrderByAcceptedAtDesc(USER_ID))
        .thenReturn(
            Optional.of(
                UserTermsAcceptance.builder().userId(USER_ID).termsVersion("v0.9").build()));

    final TermsAcceptanceStatusDto result = this.termsService.getAcceptanceStatus(USER_ID);

    assertTrue(result.isMustReaccept());
  }

  @Test
  void testGetAcceptanceStatusWhenCurrentVersionReturnsNoReacceptNeeded() {
    when(this.mockUserTermsAcceptanceRepository.findTopByUserIdOrderByAcceptedAtDesc(USER_ID))
        .thenReturn(
            Optional.of(
                UserTermsAcceptance.builder().userId(USER_ID).termsVersion("v1.0").build()));

    final TermsAcceptanceStatusDto result = this.termsService.getAcceptanceStatus(USER_ID);

    assertFalse(result.isMustReaccept());
  }

  @Test
  void testRecordAcceptanceSavesCurrentVersionForUser() {
    this.termsService.recordAcceptance(USER_ID);

    final ArgumentCaptor<UserTermsAcceptance> captor =
        ArgumentCaptor.forClass(UserTermsAcceptance.class);
    verify(this.mockUserTermsAcceptanceRepository).save(captor.capture());
    assertEquals(USER_ID, captor.getValue().getUserId());
    assertEquals("v1.0", captor.getValue().getTermsVersion());
  }
}
