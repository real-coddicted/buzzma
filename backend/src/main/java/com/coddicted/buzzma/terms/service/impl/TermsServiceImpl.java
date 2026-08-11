package com.coddicted.buzzma.terms.service.impl;

import com.coddicted.buzzma.shared.util.FileUtils;
import com.coddicted.buzzma.terms.dto.TermsAcceptanceStatusDto;
import com.coddicted.buzzma.terms.dto.TermsDto;
import com.coddicted.buzzma.terms.entity.UserTermsAcceptance;
import com.coddicted.buzzma.terms.persistence.UserTermsAcceptanceRepository;
import com.coddicted.buzzma.terms.service.TermsService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TermsServiceImpl implements TermsService {

  /**
   * Bump this whenever the terms content changes in a legally meaningful way, so accepted users are
   * asked to reaccept. A typo fix doesn't need a bump; a new clause does. Bumping requires adding a
   * new {@code /static/terms-<version>.html} file rather than editing the existing one in place —
   * old version files are never overwritten, so a user's recorded {@code termsVersion} always
   * resolves back to the exact text they accepted.
   */
  private static final String CURRENT_VERSION = "v1.0";

  private final UserTermsAcceptanceRepository userTermsAcceptanceRepository;

  public TermsServiceImpl(final UserTermsAcceptanceRepository userTermsAcceptanceRepository) {
    this.userTermsAcceptanceRepository = userTermsAcceptanceRepository;
  }

  @Override
  public TermsDto getCurrent() {
    return TermsDto.builder()
        .content(FileUtils.loadResourceAsString(resourcePathForVersion(CURRENT_VERSION)))
        .version(CURRENT_VERSION)
        .build();
  }

  private static String resourcePathForVersion(final String version) {
    return "/static/terms-" + version + ".html";
  }

  @Override
  @Transactional(readOnly = true)
  public TermsAcceptanceStatusDto getAcceptanceStatus(final UUID userId) {
    final boolean mustReaccept =
        this.userTermsAcceptanceRepository
            .findTopByUserIdOrderByAcceptedAtDesc(userId)
            .map(acceptance -> !CURRENT_VERSION.equals(acceptance.getTermsVersion()))
            .orElse(true);
    return TermsAcceptanceStatusDto.builder().mustReaccept(mustReaccept).build();
  }

  @Override
  @Transactional
  public void recordAcceptance(final UUID userId) {
    this.userTermsAcceptanceRepository.save(
        UserTermsAcceptance.builder()
            .userId(userId)
            .termsVersion(CURRENT_VERSION)
            .acceptedAt(Instant.now())
            .build());
  }
}
