package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.dto.CampaignDraftResponseDto;
import com.coddicted.buzzma.campaign.dto.CreateDraftRequestDto;
import com.coddicted.buzzma.campaign.dto.UpdateDraftRequestDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignDraft;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.persistence.CampaignDraftRepository;
import com.coddicted.buzzma.campaign.service.CampaignDraftService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignDraftServiceImpl implements CampaignDraftService {

  private final CampaignDraftRepository campaignDraftRepository;
  private final CodeGenerationService codeGenerationService;
  private final CampaignService campaignService;

  public CampaignDraftServiceImpl(
      final CampaignDraftRepository campaignDraftRepository,
      final CodeGenerationService codeGenerationService,
      final CampaignService campaignService) {
    this.campaignDraftRepository = campaignDraftRepository;
    this.codeGenerationService = codeGenerationService;
    this.campaignService = campaignService;
  }

  @Override
  @Transactional
  public CampaignDraftResponseDto create(
      final UUID requesterId, final CreateDraftRequestDto request) {
    final UUID id = UUID.randomUUID();
    final String code =
        this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN);
    final CampaignDraftResponseDto response =
        CampaignDraftResponseDto.builder()
            .id(id)
            .code(code)
            .title(request.getTitle())
            .ownerId(request.getOwnerId())
            .platform(request.getPlatform())
            .category(request.getCategory())
            .productName(request.getProductName())
            .productImageUrl(request.getProductImageUrl())
            .productUrl(request.getProductUrl())
            .productBrandName(request.getProductBrandName())
            .originalPricePaise(request.getOriginalPricePaise())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .campaignType(request.getCampaignType())
            .status(CampaignStatus.CAMPAIGN_STATUS_DRAFT)
            .campaignPricePaise(request.getCampaignPricePaise())
            .totalSlots(request.getTotalSlots())
            .returnWindowDays(request.getReturnWindowDays())
            .assignees(request.getAssignees())
            .openToAll(request.isOpenToAll())
            .affiliateLinkAllowed(request.isAffiliateLinkAllowed())
            .commissionToAllPaise(request.getCommissionToAllPaise())
            .termsAndConditions(request.getTermsAndConditions())
            .sellerName(request.getSellerName())
            .requiredSteps(request.getRequiredSteps())
            .rewards(request.getRewards())
            .exchangeProducts(request.getExchangeProducts())
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    final CampaignDraft saved =
        this.campaignDraftRepository.save(
            CampaignDraft.builder()
                .id(id)
                .code(code)
                .responseJson(response)
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .build());
    return saved.getResponseJson();
  }

  @Override
  @Transactional
  public CampaignDraftResponseDto update(
      final UUID requesterId, final UUID id, final UpdateDraftRequestDto request) {
    final CampaignDraft existing = mustFind(id);
    final CampaignDraftResponseDto current = existing.getResponseJson();
    final CampaignDraftResponseDto merged =
        current.toBuilder()
            .title(request.getTitle() != null ? request.getTitle() : current.getTitle())
            .ownerId(request.getOwnerId() != null ? request.getOwnerId() : current.getOwnerId())
            .platform(request.getPlatform() != null ? request.getPlatform() : current.getPlatform())
            .category(request.getCategory() != null ? request.getCategory() : current.getCategory())
            .productName(
                request.getProductName() != null
                    ? request.getProductName()
                    : current.getProductName())
            .productImageUrl(
                request.getProductImageUrl() != null
                    ? request.getProductImageUrl()
                    : current.getProductImageUrl())
            .productUrl(
                request.getProductUrl() != null ? request.getProductUrl() : current.getProductUrl())
            .productBrandName(
                request.getProductBrandName() != null
                    ? request.getProductBrandName()
                    : current.getProductBrandName())
            .originalPricePaise(
                request.getOriginalPricePaise() != null
                    ? request.getOriginalPricePaise()
                    : current.getOriginalPricePaise())
            .startDate(
                request.getStartDate() != null ? request.getStartDate() : current.getStartDate())
            .endDate(request.getEndDate() != null ? request.getEndDate() : current.getEndDate())
            .campaignType(
                request.getCampaignType() != null
                    ? request.getCampaignType()
                    : current.getCampaignType())
            .campaignPricePaise(
                request.getCampaignPricePaise() != null
                    ? request.getCampaignPricePaise()
                    : current.getCampaignPricePaise())
            .totalSlots(
                request.getTotalSlots() != null ? request.getTotalSlots() : current.getTotalSlots())
            .returnWindowDays(
                request.getReturnWindowDays() != null
                    ? request.getReturnWindowDays()
                    : current.getReturnWindowDays())
            .assignees(
                request.getAssignees() != null ? request.getAssignees() : current.getAssignees())
            .openToAll(
                request.getOpenToAll() != null ? request.getOpenToAll() : current.isOpenToAll())
            .affiliateLinkAllowed(
                request.getAffiliateLinkAllowed() != null
                    ? request.getAffiliateLinkAllowed()
                    : current.isAffiliateLinkAllowed())
            .commissionToAllPaise(
                request.getCommissionToAllPaise() != null
                    ? request.getCommissionToAllPaise()
                    : current.getCommissionToAllPaise())
            .termsAndConditions(
                request.getTermsAndConditions() != null
                    ? request.getTermsAndConditions()
                    : current.getTermsAndConditions())
            .sellerName(
                request.getSellerName() != null ? request.getSellerName() : current.getSellerName())
            .requiredSteps(
                request.getRequiredSteps() != null
                    ? request.getRequiredSteps()
                    : current.getRequiredSteps())
            .rewards(request.getRewards() != null ? request.getRewards() : current.getRewards())
            .exchangeProducts(
                request.getExchangeProducts() != null
                    ? request.getExchangeProducts()
                    : current.getExchangeProducts())
            .updatedBy(requesterId)
            .build();
    final CampaignDraft saved =
        this.campaignDraftRepository.save(existing.toBuilder().responseJson(merged).build());
    return saved.getResponseJson();
  }

  @Override
  @Transactional
  public CampaignDraftResponseDto copyFromCampaign(final UUID campaignId, final UUID requesterId) {
    final Campaign src = this.campaignService.getById(campaignId);
    final Product product = src.getProduct();
    final UUID id = UUID.randomUUID();
    final String code =
        this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN);
    final CampaignDraftResponseDto response =
        CampaignDraftResponseDto.builder()
            .id(id)
            .code(code)
            .title(src.getTitle() + " (Copy)")
            .ownerId(src.getOwnerId())
            .platform(src.getPlatform())
            .category(src.getCategory())
            .productName(product.getName())
            .productImageUrl(
                product.getImageUrl() != null ? product.getImageUrl().toString() : null)
            .productUrl(
                product.getProductLink() != null ? product.getProductLink().toString() : null)
            .productBrandName(product.getBrandName())
            .originalPricePaise(product.getPricePaise())
            .startDate(src.getStartDate())
            .endDate(src.getEndDate())
            .campaignType(src.getType())
            .status(CampaignStatus.CAMPAIGN_STATUS_DRAFT)
            .campaignPricePaise(src.getCampaignPricePaise())
            .totalSlots(src.getTotalSlots())
            .returnWindowDays(src.getReturnWindowDays())
            .assignees(null)
            .openToAll(src.isOpenToAll())
            .affiliateLinkAllowed(src.isAffiliateLinkAllowed())
            .commissionToAllPaise(src.getCommissionToAllPaise())
            .termsAndConditions(src.getTermsAndConditions())
            .sellerName(src.getSellerName())
            .requiredSteps(src.getRequiredSteps())
            .rewards(src.getRewards())
            .exchangeProducts(src.getExchangeProducts())
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    final CampaignDraft saved =
        this.campaignDraftRepository.save(
            CampaignDraft.builder()
                .id(id)
                .code(code)
                .responseJson(response)
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .build());
    return saved.getResponseJson();
  }

  @Override
  @Transactional(readOnly = true)
  public CampaignDraftResponseDto getById(final UUID id) {
    return mustFind(id).getResponseJson();
  }

  @Override
  @Transactional(readOnly = true)
  public CampaignDraft getEntityById(final UUID id) {
    return mustFind(id);
  }

  @Override
  @Transactional
  public void delete(final UUID requesterId, final UUID id) {
    final CampaignDraft draft = mustFind(id);
    if (!requesterId.equals(draft.getResponseJson().getOwnerId())) {
      throw new ForbiddenException("Only the campaign draft owner can delete it");
    }
    this.campaignDraftRepository.delete(draft);
  }

  private CampaignDraft mustFind(final UUID id) {
    return this.campaignDraftRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Campaign draft not found: " + id));
  }
}
