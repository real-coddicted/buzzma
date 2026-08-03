package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetDownloadDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheet;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ClaimReviewWorksheetService {

  ClaimReviewWorksheet uploadWorksheet(BuzzmaUser uploader, MultipartFile file);

  ClaimReviewWorksheetDownloadDto downloadWorksheet(UUID id);
}
