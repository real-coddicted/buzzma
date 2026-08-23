package com.coddicted.buzzma.support.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.support.dto.TicketResponseDto;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.entity.TicketStatus;
import com.coddicted.buzzma.support.entity.TicketSubCategory;
import com.coddicted.buzzma.support.mapper.TicketAttachmentMapper;
import com.coddicted.buzzma.support.mapper.TicketCommentMapper;
import com.coddicted.buzzma.support.mapper.TicketMapper;
import com.coddicted.buzzma.support.mapper.TicketMapperImpl;
import com.coddicted.buzzma.support.service.TicketAttachmentService;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import com.coddicted.buzzma.support.service.TicketCommentService;
import com.coddicted.buzzma.support.service.TicketService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TicketControllerTest {

  private static final UUID REQUESTER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID TICKET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CATEGORY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID SUB_CATEGORY_ID =
      UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final String RAISED_BY_NAME = "Test Buyer";

  private TicketService ticketService;
  private TicketCategoryService ticketCategoryService;
  private UserService userService;
  private TicketController controller;

  @BeforeEach
  void setUp() {
    this.ticketService = Mockito.mock(TicketService.class);
    this.ticketCategoryService = Mockito.mock(TicketCategoryService.class);
    this.userService = Mockito.mock(UserService.class);
    final TicketMapper ticketMapper = new TicketMapperImpl();
    this.controller =
        new TicketController(
            this.ticketService,
            Mockito.mock(TicketCommentService.class),
            Mockito.mock(TicketAttachmentService.class),
            ticketMapper,
            Mockito.mock(TicketCommentMapper.class),
            Mockito.mock(TicketAttachmentMapper.class),
            this.userService,
            this.ticketCategoryService);
  }

  @Test
  void testGetByIdResolvesCategoryAndSubCategoryNamesRegardlessOfRequesterRole() {
    final Ticket ticket =
        Ticket.builder()
            .id(TICKET_ID)
            .categoryId(CATEGORY_ID)
            .subCategoryId(SUB_CATEGORY_ID)
            .raisedBy(REQUESTER_ID)
            .title("Order not delivered")
            .description("test description")
            .status(TicketStatus.TICKET_STATUS_IN_PROGRESS)
            .build();
    when(this.ticketService.getById(TICKET_ID)).thenReturn(ticket);

    when(this.userService.getNamesByIds(List.of(REQUESTER_ID)))
        .thenReturn(Map.of(REQUESTER_ID, RAISED_BY_NAME));

    when(this.ticketCategoryService.getById(CATEGORY_ID))
        .thenReturn(TicketCategory.builder().id(CATEGORY_ID).name("Claim").build());
    when(this.ticketCategoryService.getTicketSubCategory(CATEGORY_ID, SUB_CATEGORY_ID))
        .thenReturn(
            TicketSubCategory.builder()
                .id(SUB_CATEGORY_ID)
                .categoryId(CATEGORY_ID)
                .name("Product Issue")
                .build());

    final TicketResponseDto response = this.controller.getById(REQUESTER_ID, TICKET_ID);

    assertThat(response.getCategoryName()).isEqualTo("Claim");
    assertThat(response.getSubCategoryName()).isEqualTo("Product Issue");
    assertThat(response.getTitle()).isEqualTo("Order not delivered");
    assertThat(response.getRaisedByName()).isEqualTo(RAISED_BY_NAME);
  }
}
