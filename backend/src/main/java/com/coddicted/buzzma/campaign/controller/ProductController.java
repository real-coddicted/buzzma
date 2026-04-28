package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.identity.entity.UsersEntity;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

  private final DealRepository dealRepository;
  private final UsersRepository usersRepository;

  public ProductController(DealRepository dealRepository, UsersRepository usersRepository) {
    this.dealRepository = dealRepository;
    this.usersRepository = usersRepository;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @CurrentUserId UUID actorId) {

    UsersEntity user =
        usersRepository
            .findById(actorId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));

    String mediatorCode = user.getParentCode();
    if (mediatorCode == null || mediatorCode.isBlank()) {
      return Map.of("data", List.of(), "total", 0, "page", page, "limit", limit);
    }

    PageRequest pageable = PageRequest.of(page - 1, limit);
    Page<Deal> results =
        dealRepository.findActiveProductsForMediator(mediatorCode, pageable);

    List<Map<String, Object>> products =
        results.getContent().stream()
            .map(
                d -> {
                  Map<String, Object> p = new LinkedHashMap<>();
                  p.put("id", d.getId());
                  p.put("campaignId", d.getCampaignId());
                  p.put("mediatorCode", d.getMediatorCode());
                  p.put("title", d.getTitle());
                  p.put("description", d.getDescription() != null ? d.getDescription() : "");
                  p.put("image", d.getImage());
                  p.put("productUrl", d.getProductUrl());
                  p.put("platform", d.getPlatform());
                  p.put("brandName", d.getBrandName());
                  p.put("dealType", d.getDealType() != null ? d.getDealType().name() : "Rating");
                  p.put("price", d.getPricePaise() != null ? d.getPricePaise() / 100.0 : 0);
                  p.put(
                      "originalPrice",
                      d.getOriginalPricePaise() != null ? d.getOriginalPricePaise() / 100.0 : 0);
                  p.put(
                      "commission",
                      d.getCommissionPaise() != null ? d.getCommissionPaise() / 100.0 : 0);
                  p.put("rating", d.getRating() != null ? d.getRating() : 5.0);
                  p.put("category", d.getCategory() != null ? d.getCategory() : "General");
                  p.put("active", d.getActive());
                  return p;
                })
            .toList();

    return Map.of(
        "data", products,
        "total", results.getTotalElements(),
        "page", page,
        "limit", limit);
  }
}
