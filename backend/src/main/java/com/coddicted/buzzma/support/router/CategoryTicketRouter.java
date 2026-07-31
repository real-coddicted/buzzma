package com.coddicted.buzzma.support.router;

import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketCategoryCode;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Routes a ticket to an assignee based on its category.
 *
 * <p>Routing rules:
 *
 * <ol>
 *   <li><b>Technical tickets</b> ({@code TICKET_CATEGORY_TECHNICAL}) — always assigned to the admin
 *       user ({@link UserRole#ROLE_ADMIN}), regardless of who raised the ticket.
 *   <li><b>Claim tickets</b> ({@code TICKET_CATEGORY_CLAIM}) — assigned to the owner of the deal
 *       backing the ticket's {@code claimCode} (the mediator).
 *   <li><b>Campaign tickets</b> ({@code TICKET_CATEGORY_CAMPAIGN}) — assigned to the owner of the
 *       campaign identified by the ticket's {@code campaignCode}.
 *   <li><b>Any other category</b> — assigned to the {@code fromUser} of the accepted connection
 *       whose {@code toUserId} matches the ticket raiser. This is the person who invited the raiser
 *       into the platform and is therefore their primary point of contact.
 * </ol>
 */
@Component
public class CategoryTicketRouter implements TicketRouter {

  private final TicketCategoryService ticketCategoryService;
  private final ConnectionService connectionService;
  private final UserService userService;
  private final ClaimService claimService;
  private final DealService dealService;
  private final CampaignService campaignService;

  public CategoryTicketRouter(
      final TicketCategoryService ticketCategoryService,
      final ConnectionService connectionService,
      final UserService userService,
      final ClaimService claimService,
      final DealService dealService,
      final CampaignService campaignService) {
    this.ticketCategoryService = ticketCategoryService;
    this.connectionService = connectionService;
    this.userService = userService;
    this.claimService = claimService;
    this.dealService = dealService;
    this.campaignService = campaignService;
  }

  /**
   * Resolves the assignee for the given ticket and returns a copy with {@code assigneeId}
   * populated.
   *
   * @param ticket a fully prepared ticket with {@code categoryId} and {@code raisedBy} set
   * @return a copy of the ticket with {@code assigneeId} set to the resolved assignee
   * @throws com.coddicted.buzzma.shared.exception.NotFoundException if no admin user exists
   *     (technical category), the claim/campaign referenced by its code cannot be found, or no
   *     accepted connection exists for the raiser (other categories)
   */
  @Override
  public Ticket route(final Ticket ticket) {
    final String categoryCode =
        this.ticketCategoryService.getById(ticket.getCategoryId()).getCode();
    final UUID assigneeId;
    if (TicketCategoryCode.TECHNICAL.equals(categoryCode)) {
      assigneeId = this.userService.getByRole(UserRole.ROLE_ADMIN).getId();
    } else if (TicketCategoryCode.CLAIM.equals(categoryCode)) {
      final Claim claim = this.claimService.getByCode(ticket.getClaimCode());
      assigneeId = this.dealService.getById(claim.getDealId()).getOwnerId();
    } else if (TicketCategoryCode.CAMPAIGN.equals(categoryCode)) {
      assigneeId = this.campaignService.getByCode(ticket.getCampaignCode()).getOwnerId();
    } else {
      assigneeId =
          this.connectionService
              .getConnectionByToUserIdAndStatus(
                  ticket.getRaisedBy(), ConnectionStatus.CONNECTION_STATUS_ACCEPTED)
              .getFromUserId();
    }
    return ticket.toBuilder().assigneeId(assigneeId).build();
  }
}
