package com.coddicted.buzzma.campaign.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.mapper.DealMapper;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.model.ConnectionView;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class DealControllerTest {

  private static final UUID REQUESTER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID MEDIATOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID DEAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final String MEDIATOR_NAME = "Alice Mediator";

  private DealService dealService;
  private ConnectionService connectionService;
  private DealMapper dealMapper;
  private UserService userService;
  private DealController controller;

  @BeforeEach
  void setUp() {
    this.dealService = Mockito.mock(DealService.class);
    this.connectionService = Mockito.mock(ConnectionService.class);
    this.dealMapper = Mockito.mock(DealMapper.class);
    this.userService = Mockito.mock(UserService.class);
    this.controller =
        new DealController(
            this.dealService, this.connectionService, this.dealMapper, this.userService);
  }

  @Test
  void testGetActiveDealsEnrichesItemsWithOwnerName() {
    final Connection connection =
        Connection.builder()
            .fromUserId(MEDIATOR_ID)
            .toUserId(REQUESTER_ID)
            .status(ConnectionStatus.CONNECTION_STATUS_ACCEPTED)
            .build();
    final ConnectionView connectionView = ConnectionView.builder().connection(connection).build();
    when(this.connectionService.getConnectionsByToUserIdAndStatus(
            REQUESTER_ID, ConnectionStatus.CONNECTION_STATUS_ACCEPTED))
        .thenReturn(Set.of(connectionView));

    final Deal deal = Deal.builder().id(DEAL_ID).ownerId(MEDIATOR_ID).build();
    when(this.dealService.getActiveDeals(Set.of(MEDIATOR_ID), REQUESTER_ID, 0, 20))
        .thenReturn(new PageImpl<>(List.of(deal), PageRequest.of(0, 20), 1));

    final DealResponseDto mappedDto =
        DealResponseDto.builder().id(DEAL_ID).ownerId(MEDIATOR_ID).build();
    when(this.dealMapper.toDealResponse(List.of(deal))).thenReturn(List.of(mappedDto));

    when(this.userService.getNamesByIds(Set.of(MEDIATOR_ID)))
        .thenReturn(Map.of(MEDIATOR_ID, MEDIATOR_NAME));

    final var result = this.controller.getActiveDeals(REQUESTER_ID, 0, 20);

    assertThat(result.getItems()).hasSize(1);
    assertThat(result.getItems().get(0).getOwnerName()).isEqualTo(MEDIATOR_NAME);
  }

  @Test
  void testGetActiveDealsWithNoConnectionsReturnsEmptyPageWithoutLookup() {
    when(this.connectionService.getConnectionsByToUserIdAndStatus(
            REQUESTER_ID, ConnectionStatus.CONNECTION_STATUS_ACCEPTED))
        .thenReturn(Set.of());

    final var result = this.controller.getActiveDeals(REQUESTER_ID, 0, 20);

    assertThat(result.getItems()).isEmpty();
    assertThat(result.getTotal()).isZero();
  }
}
