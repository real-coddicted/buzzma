package com.coddicted.buzzma.support.controller;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.support.dto.TicketCategoryResponseDto;
import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.mapper.TicketCategoryMapper;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import java.util.List;
import java.util.UUID;
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
  private final UserService userService;

  public TicketCategoryController(
      final TicketCategoryService ticketCategoryService,
      final TicketCategoryMapper ticketCategoryMapper,
      final UserService userService) {
    this.ticketCategoryService = ticketCategoryService;
    this.ticketCategoryMapper = ticketCategoryMapper;
    this.userService = userService;
  }

  @GetMapping
  public List<TicketCategoryResponseDto> list(@CurrentUserId final UUID requesterId) {
    final UserRole requesterRole = this.userService.getById(requesterId).getRole();
    final List<TicketCategory> categories =
        this.ticketCategoryService.listActiveCategories(requesterRole);
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
