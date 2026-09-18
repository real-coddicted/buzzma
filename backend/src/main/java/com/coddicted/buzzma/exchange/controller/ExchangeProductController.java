package com.coddicted.buzzma.exchange.controller;

import com.coddicted.buzzma.exchange.dto.ExchangeProductRequestDto;
import com.coddicted.buzzma.exchange.dto.ExchangeProductResponseDto;
import com.coddicted.buzzma.exchange.entity.AgencyExchangeProduct;
import com.coddicted.buzzma.exchange.mapper.ExchangeProductMapper;
import com.coddicted.buzzma.exchange.service.ExchangeProductService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agency/exchange-products")
@Validated
@PreAuthorize(UserRole.Expr.AGENCY)
public class ExchangeProductController {

  private final ExchangeProductService exchangeProductService;
  private final ExchangeProductMapper exchangeProductMapper;

  public ExchangeProductController(
      final ExchangeProductService exchangeProductService,
      final ExchangeProductMapper exchangeProductMapper) {
    this.exchangeProductService = exchangeProductService;
    this.exchangeProductMapper = exchangeProductMapper;
  }

  @GetMapping
  public List<ExchangeProductResponseDto> list(@CurrentUserId final UUID agencyId) {
    return this.exchangeProductService.list(agencyId).stream()
        .map(this.exchangeProductMapper::toResponse)
        .toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExchangeProductResponseDto create(
      @CurrentUserId final UUID agencyId,
      @Valid @RequestBody final ExchangeProductRequestDto request) {
    return this.exchangeProductMapper.toResponse(
        this.exchangeProductService.create(this.exchangeProductMapper.toEntity(request), agencyId));
  }

  @PutMapping("/{id}")
  public ExchangeProductResponseDto update(
      @CurrentUserId final UUID agencyId,
      @PathVariable final UUID id,
      @Valid @RequestBody final ExchangeProductRequestDto request) {
    final AgencyExchangeProduct patch =
        this.exchangeProductMapper.toEntity(request).toBuilder().id(id).build();
    return this.exchangeProductMapper.toResponse(
        this.exchangeProductService.update(patch, agencyId));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@CurrentUserId final UUID agencyId, @PathVariable final UUID id) {
    this.exchangeProductService.delete(id, agencyId);
  }
}
