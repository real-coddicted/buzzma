package com.coddicted.buzzma.terms.service;

import com.coddicted.buzzma.terms.dto.TermsAcceptanceStatusDto;
import com.coddicted.buzzma.terms.dto.TermsDto;
import java.util.UUID;

public interface TermsService {

  TermsDto getCurrent();

  TermsAcceptanceStatusDto getAcceptanceStatus(UUID userId);

  void recordAcceptance(UUID userId);
}
