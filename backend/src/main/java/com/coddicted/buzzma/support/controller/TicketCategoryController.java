package com.coddicted.buzzma.support.controller;

import com.coddicted.buzzma.support.dto.TicketCategoryResponseDto;
import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.mapper.TicketCategoryMapper;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ticket-categories")
@Validated
public class TicketCategoryController {

  private final TicketCategoryService ticketCategoryService;
  private final TicketCategoryMapper ticketCategoryMapper;

  public TicketCategoryController(
      final TicketCategoryService ticketCategoryService,
      final TicketCategoryMapper ticketCategoryMapper) {
    this.ticketCategoryService = ticketCategoryService;
    this.ticketCategoryMapper = ticketCategoryMapper;
  }

  @GetMapping
  public List<TicketCategoryResponseDto> list() {
    final List<TicketCategory> categories = this.ticketCategoryService.listActiveCategories();
    return categories.stream()
        .map(
            category ->
                this.ticketCategoryMapper.toResponse(
                    category,
                    this.ticketCategoryService.listActiveSubCategoriesByCategoryId(
                        category.getId())))
        .toList();
  }
}
