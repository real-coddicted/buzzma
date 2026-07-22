package com.coddicted.buzzma.report.service;

import com.coddicted.buzzma.claim.dto.ClaimReviewFilterRequestDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;

public interface ReportService {

  byte[] generateClaimReviewReport(BuzzmaUser requester, ClaimReviewFilterRequestDto filter);
}
