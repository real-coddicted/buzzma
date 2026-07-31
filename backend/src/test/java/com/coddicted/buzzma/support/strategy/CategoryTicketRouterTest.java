package com.coddicted.buzzma.support.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.router.CategoryTicketRouter;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryTicketRouterTest {

  private static final UUID TECHNICAL_CATEGORY_ID =
      UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID CLAIM_CATEGORY_ID =
      UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final UUID CAMPAIGN_CATEGORY_ID =
      UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
  private static final UUID GENERAL_CATEGORY_ID =
      UUID.fromString("11111111-2222-3333-4444-555555555555");
  private static final UUID RAISER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
  private static final UUID ADMIN_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
  private static final UUID FROM_USER_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
  private static final UUID DEAL_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final UUID MEDIATOR_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
  private static final UUID CAMPAIGN_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
  private static final UUID BRAND_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

  private static final TicketCategory TECHNICAL_CATEGORY =
      TicketCategory.builder().id(TECHNICAL_CATEGORY_ID).code("TICKET_CATEGORY_TECHNICAL").build();

  private static final TicketCategory CLAIM_CATEGORY =
      TicketCategory.builder().id(CLAIM_CATEGORY_ID).code("TICKET_CATEGORY_CLAIM").build();

  private static final TicketCategory CAMPAIGN_CATEGORY =
      TicketCategory.builder().id(CAMPAIGN_CATEGORY_ID).code("TICKET_CATEGORY_CAMPAIGN").build();

  private static final TicketCategory GENERAL_CATEGORY =
      TicketCategory.builder().id(GENERAL_CATEGORY_ID).code("TICKET_CATEGORY_GENERAL").build();

  private static final BuzzmaUser ADMIN_USER =
      BuzzmaUser.builder().id(ADMIN_ID).role(UserRole.ROLE_ADMIN).build();

  private static final Connection ACCEPTED_CONNECTION =
      Connection.builder().fromUserId(FROM_USER_ID).toUserId(RAISER_ID).build();

  private static final Claim CLAIM = Claim.builder().dealId(DEAL_ID).build();
  private static final Deal DEAL = Deal.builder().id(DEAL_ID).ownerId(MEDIATOR_ID).build();
  private static final Campaign CAMPAIGN =
      Campaign.builder().id(CAMPAIGN_ID).ownerId(BRAND_ID).build();

  @Mock private TicketCategoryService mockTicketCategoryService;
  @Mock private ConnectionService mockConnectionService;
  @Mock private UserService mockUserService;
  @Mock private ClaimService mockClaimService;
  @Mock private DealService mockDealService;
  @Mock private CampaignService mockCampaignService;
  private CategoryTicketRouter router;

  @BeforeEach
  void setUp() {
    this.router =
        new CategoryTicketRouter(
            this.mockTicketCategoryService,
            this.mockConnectionService,
            this.mockUserService,
            this.mockClaimService,
            this.mockDealService,
            this.mockCampaignService);
  }

  @Test
  void route_technicalCategory_assignsAdminUser() {
    final Ticket ticket =
        Ticket.builder().categoryId(TECHNICAL_CATEGORY_ID).raisedBy(RAISER_ID).build();
    when(this.mockTicketCategoryService.getById(TECHNICAL_CATEGORY_ID))
        .thenReturn(TECHNICAL_CATEGORY);
    when(this.mockUserService.getByRole(UserRole.ROLE_ADMIN)).thenReturn(ADMIN_USER);

    final Ticket routed = this.router.route(ticket);

    assertEquals(ADMIN_ID, routed.getAssigneeId());
  }

  @Test
  void route_claimCategory_assignsDealOwner() {
    final Ticket ticket =
        Ticket.builder()
            .categoryId(CLAIM_CATEGORY_ID)
            .raisedBy(RAISER_ID)
            .claimCode("CLM1-A2B3")
            .build();
    when(this.mockTicketCategoryService.getById(CLAIM_CATEGORY_ID)).thenReturn(CLAIM_CATEGORY);
    when(this.mockClaimService.getByCode("CLM1-A2B3")).thenReturn(CLAIM);
    when(this.mockDealService.getById(DEAL_ID)).thenReturn(DEAL);

    final Ticket routed = this.router.route(ticket);

    assertEquals(MEDIATOR_ID, routed.getAssigneeId());
  }

  @Test
  void route_campaignCategory_assignsCampaignOwner() {
    final Ticket ticket =
        Ticket.builder()
            .categoryId(CAMPAIGN_CATEGORY_ID)
            .raisedBy(RAISER_ID)
            .campaignCode("CMP-001")
            .build();
    when(this.mockTicketCategoryService.getById(CAMPAIGN_CATEGORY_ID))
        .thenReturn(CAMPAIGN_CATEGORY);
    when(this.mockCampaignService.getByCode("CMP-001")).thenReturn(CAMPAIGN);

    final Ticket routed = this.router.route(ticket);

    assertEquals(BRAND_ID, routed.getAssigneeId());
  }

  @Test
  void route_otherCategory_assignsConnectionFromUser() {
    final Ticket ticket =
        Ticket.builder().categoryId(GENERAL_CATEGORY_ID).raisedBy(RAISER_ID).build();
    when(this.mockTicketCategoryService.getById(GENERAL_CATEGORY_ID)).thenReturn(GENERAL_CATEGORY);
    when(this.mockConnectionService.getConnectionByToUserIdAndStatus(
            RAISER_ID, ConnectionStatus.CONNECTION_STATUS_ACCEPTED))
        .thenReturn(ACCEPTED_CONNECTION);

    final Ticket routed = this.router.route(ticket);

    assertEquals(FROM_USER_ID, routed.getAssigneeId());
  }
}
