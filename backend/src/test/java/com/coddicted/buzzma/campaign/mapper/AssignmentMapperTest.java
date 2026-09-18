package com.coddicted.buzzma.campaign.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.coddicted.buzzma.campaign.dto.AssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.AssignmentSummaryResponseDto;
import com.coddicted.buzzma.campaign.dto.AssignmentSummaryView;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.model.Assignment;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AssignmentMapperTest {

  private final AssignmentMapper assignmentMapper = Mappers.getMapper(AssignmentMapper.class);

  @Test
  void toResponseMapsCampaignStartAndEndDate() throws MalformedURLException, URISyntaxException {
    final Product product =
        Product.builder()
            .name("Test Product")
            .productLink(new URI("https://example.com/product").toURL())
            .pricePaise(BigInteger.valueOf(99900))
            .build();
    final Campaign campaign =
        Campaign.builder().product(product).startDate(20260901).endDate(20260930).build();
    final Assignment assignment =
        Assignment.builder()
            .campaign(campaign)
            .campaignAssignment(new CampaignAssignment())
            .build();

    final AssignmentResponseDto response = this.assignmentMapper.toResponse(assignment);

    assertEquals(20260901, response.getStartDate());
    assertEquals(20260930, response.getEndDate());
  }

  @Test
  void toResponseMapsCampaignOwnerIdToAgencyId() throws MalformedURLException, URISyntaxException {
    final UUID agencyId = UUID.randomUUID();
    final Product product =
        Product.builder()
            .name("Test Product")
            .productLink(new URI("https://example.com/product").toURL())
            .pricePaise(BigInteger.valueOf(99900))
            .build();
    final Campaign campaign = Campaign.builder().product(product).ownerId(agencyId).build();
    final Assignment assignment =
        Assignment.builder()
            .campaign(campaign)
            .campaignAssignment(new CampaignAssignment())
            .build();

    final AssignmentResponseDto response = this.assignmentMapper.toResponse(assignment);

    assertEquals(agencyId, response.getAgencyId());
  }

  @Test
  void toSummaryResponseMapsCampaignStartAndEndDate() {
    final AssignmentSummaryView view =
        new AssignmentSummaryView(
            null, null, null, null, null, null, null, null, null, null, null, 20260901, 20260930,
            null);

    final AssignmentSummaryResponseDto response = this.assignmentMapper.toSummaryResponse(view);

    assertEquals(20260901, response.getStartDate());
    assertEquals(20260930, response.getEndDate());
  }

  @Test
  void toSummaryResponseMapsCampaignOwnerIdToAgencyId() {
    final UUID agencyId = UUID.randomUUID();
    final AssignmentSummaryView view =
        new AssignmentSummaryView(
            null, null, null, null, null, null, null, null, null, null, null, null, null, agencyId);

    final AssignmentSummaryResponseDto response = this.assignmentMapper.toSummaryResponse(view);

    assertEquals(agencyId, response.getAgencyId());
  }
}
