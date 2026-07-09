package com.coddicted.buzzma.claim.controller;

import com.coddicted.buzzma.campaign.entity.CampaignTypeStep;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.service.CampaignTypeStepService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.dto.ClaimRequestDto;
import com.coddicted.buzzma.claim.dto.ClaimResponseDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewRequestDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.claim.dto.ScreenshotReviewRequestDto;
import com.coddicted.buzzma.claim.dto.UpdateClaimRequestDto;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.mapper.ClaimMapper;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.claim.processor.ClaimReviewProcessor;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.shared.security.CurrentUser;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimController.class);

  private final ClaimService claimService;
  private final DealService dealService;
  private final CampaignTypeStepService campaignTypeStepService;
  private final ClaimMapper claimMapper;
  private final ClaimReviewProcessor claimReviewProcessor;

  public ClaimController(
      final ClaimService claimService,
      final DealService dealService,
      final CampaignTypeStepService campaignTypeStepService,
      final ClaimMapper claimMapper,
      final ClaimReviewProcessor claimReviewProcessor) {
    this.claimService = claimService;
    this.dealService = dealService;
    this.campaignTypeStepService = campaignTypeStepService;
    this.claimMapper = claimMapper;
    this.claimReviewProcessor = claimReviewProcessor;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('BUYER')")
  public ClaimResponseDto create(
      @CurrentUserId final UUID requesterId, @Valid final ClaimRequestDto request) {

    LOGGER.info("Create claim request, extractedDetails: {}", request.getExtractedDetails());
    final MultipartFile screenshot = request.getScreenshot();
    final Claim claim =
        this.claimService.createClaim(
            this.claimMapper.toEntity(request, requesterId),
            readBytes(screenshot),
            screenshot.getOriginalFilename(),
            screenshot.getContentType(),
            request.getExtractedDetails(),
            request.getOverallScore());
    final Deal deal = this.dealService.getById(claim.getDealId());
    final List<ClaimScreenshot> screenshots = this.claimService.listScreenshots(claim.getId());
    return this.claimMapper.toResponse(claim, deal, screenshots, currentStep(claim, deal));
  }

  @PostMapping("/{id}/rating")
  @PreAuthorize("hasRole('BUYER')")
  public ClaimResponseDto submitRating(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @RequestParam("screenshot") final MultipartFile screenshot) {
    final ClaimWithDeal result =
        this.claimService.submitRating(
            id,
            requesterId,
            readBytes(screenshot),
            screenshot.getOriginalFilename(),
            screenshot.getContentType());
    final Claim claim = result.claim();
    final Deal deal = result.deal();
    final List<ClaimScreenshot> screenshots = this.claimService.listScreenshots(claim.getId());
    return this.claimMapper.toResponse(claim, deal, screenshots, currentStep(claim, deal));
  }

  @PostMapping("/{id}/review")
  @PreAuthorize("hasRole('BUYER')")
  public ClaimResponseDto submitReview(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @RequestParam(required = false) final String reviewUrl,
      @RequestParam("screenshot") final MultipartFile screenshot) {
    final ClaimWithDeal result =
        this.claimService.submitReview(
            id,
            requesterId,
            reviewUrl,
            readBytes(screenshot),
            screenshot.getOriginalFilename(),
            screenshot.getContentType());
    final Claim claim = result.claim();
    final Deal deal = result.deal();
    final List<ClaimScreenshot> screenshots = this.claimService.listScreenshots(claim.getId());
    return this.claimMapper.toResponse(claim, deal, screenshots, currentStep(claim, deal));
  }

  @PostMapping("/{id}/return")
  @PreAuthorize("hasRole('BUYER')")
  public ClaimResponseDto submitReturn(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @RequestParam("screenshot") final MultipartFile screenshot) {
    final ClaimWithDeal result =
        this.claimService.submitReturn(
            id,
            requesterId,
            readBytes(screenshot),
            screenshot.getOriginalFilename(),
            screenshot.getContentType());
    final Claim claim = result.claim();
    final Deal deal = result.deal();
    final List<ClaimScreenshot> screenshots = this.claimService.listScreenshots(claim.getId());
    return this.claimMapper.toResponse(claim, deal, screenshots, currentStep(claim, deal));
  }

  @PostMapping("/{id}/update")
  @PreAuthorize("hasRole('BUYER')")
  public ClaimResponseDto updateScreenshot(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @Valid final UpdateClaimRequestDto request) {
    final MultipartFile screenshot = request.getScreenshot();
    final ClaimWithDeal result =
        this.claimService.updateScreenshot(
            id,
            requesterId,
            request.getScreenshotId(),
            request.getScreenshotType(),
            readBytes(screenshot),
            screenshot.getOriginalFilename(),
            screenshot.getContentType());
    final Claim claim = result.claim();
    final Deal deal = result.deal();
    final List<ClaimScreenshot> screenshots = this.claimService.listScreenshots(claim.getId());
    return this.claimMapper.toResponse(claim, deal, screenshots, currentStep(claim, deal));
  }

  @PostMapping("/{id}/submitReview")
  @PreAuthorize("hasAnyRole('AGENCY','MEDIATOR')")
  public ClaimResponseDto submitClaimReview(
      @CurrentUser final BuzzmaUser requester,
      @PathVariable final UUID id,
      @Valid @RequestBody final ClaimReviewRequestDto request) {
    final ClaimWithDeal result =
        this.claimService.submitClaimReview(
            id,
            requester.getId(),
            requester.getRole(),
            request.getReviewerDecision(),
            request.getReviewerComment());
    final Claim claim = result.claim();
    final Deal deal = result.deal();
    final List<ClaimScreenshot> screenshots = this.claimService.listScreenshots(claim.getId());
    return this.claimMapper.toResponse(claim, deal, screenshots, currentStep(claim, deal));
  }

  @PostMapping("/screenshots/review")
  @PreAuthorize(
      "hasAnyRole('AGENCY','MEDIATOR')") // Mediators can also review the screenshots, "can be
  // removed"
  public ClaimResponseDto reviewScreenshot(
      @CurrentUserId final UUID reviewerId,
      @Valid @RequestBody final ScreenshotReviewRequestDto request) {
    final ClaimWithDeal result =
        this.claimService.reviewScreenshot(
            request.getScreenshotId(),
            request.getClaimId(),
            request.getAction(),
            reviewerId,
            request.getReviewerComments());
    final Claim claim = result.claim();
    final Deal deal = result.deal();
    final List<ClaimScreenshot> screenshots = this.claimService.listScreenshots(claim.getId());
    return this.claimMapper.toResponse(claim, deal, screenshots, currentStep(claim, deal));
  }

  @GetMapping
  @PreAuthorize("hasRole('BUYER')")
  public List<ClaimResponseDto> list(@CurrentUserId final UUID requesterId) {
    return this.claimService.listByOwner(requesterId).stream()
        .map(
            claim -> {
              final Deal deal = this.dealService.getById(claim.getDealId());
              return this.claimMapper.toResponse(
                  claim,
                  deal,
                  this.claimService.listScreenshots(claim.getId()),
                  currentStep(claim, deal));
            })
        .toList();
  }

  @GetMapping("/review")
  @PreAuthorize("hasAnyRole('AGENCY', 'MEDIATOR')")
  public Page<ClaimReviewResponseDto> listClaimsToReview(
      @CurrentUser BuzzmaUser requester, final Pageable pageable) {
    return this.claimReviewProcessor.listClaimReviews(requester, pageable);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('BUYER')")
  public ClaimResponseDto getById(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    final Claim claim = this.claimService.getById(id, requesterId);
    final Deal deal = this.dealService.getById(claim.getDealId());
    final List<ClaimScreenshot> screenshots = this.claimService.listScreenshots(claim.getId());
    return this.claimMapper.toResponse(claim, deal, screenshots, currentStep(claim, deal));
  }

  private int currentStep(final Claim claim, final Deal deal) {
    final List<CampaignTypeStep> steps =
        this.campaignTypeStepService
            .getStepConfig()
            .getOrDefault(deal.getCampaign().getType(), List.of());
    for (final CampaignTypeStep step : steps) {
      if (step.getId().getStepType() == claim.getCurrentStep()) {
        return step.getStepOrder();
      }
    }
    return 0;
  }

  private byte[] readBytes(final MultipartFile file) {
    try {
      return file.getBytes();
    } catch (final IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file");
    }
  }
}
