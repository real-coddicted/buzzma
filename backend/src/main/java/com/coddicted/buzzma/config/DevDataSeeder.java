package com.coddicted.buzzma.config;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UserStatus;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local"})
public class DevDataSeeder implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DevDataSeeder.class);

  private static final List<String> CAMPAIGN_SCENARIOS =
      List.of(
          "scenario-1",
          "scenario-2",
          "scenario-3",
          "scenario-4",
          "scenario-5",
          "scenario-6",
          "scenario-7",
          "scenario-8");

  private static final UUID ADMIN_ID = UUID.fromString("5eed0001-0000-0000-0000-000000000001");
  private static final UUID BUYER_ID = UUID.fromString("5eed0001-0000-0000-0000-000000000002");
  private static final UUID AGENCY_ID = UUID.fromString("5eed0001-0000-0000-0000-000000000003");
  private static final UUID BRAND_ID = UUID.fromString("5eed0001-0000-0000-0000-000000000004");
  private static final UUID MEDIATOR_ID = UUID.fromString("5eed0001-0000-0000-0000-000000000005");

  private final PasswordService passwordService;
  private final JdbcTemplate jdbcTemplate;

  public DevDataSeeder(final PasswordService passwordService, final JdbcTemplate jdbcTemplate) {
    this.passwordService = passwordService;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  @Transactional
  public void run(final ApplicationArguments args) {
    seedUser("Test Admin 1", "9000000001", "test1234", UserRole.ROLE_ADMIN, ADMIN_ID);
    seedUser("Test Brand 1", "9100000001", "test1234", UserRole.ROLE_BRAND, BRAND_ID);
    seedUser("Test Agency 1", "9200000001", "test1234", UserRole.ROLE_AGENCY, AGENCY_ID);
    seedUser("Test Mediator 1", "9300000001", "test1234", UserRole.ROLE_MEDIATOR, MEDIATOR_ID);
    seedUser("Test Buyer 1", "9400000001", "test1234", UserRole.ROLE_BUYER, BUYER_ID);

    seedConnections();
    //    CAMPAIGN_SCENARIOS.forEach(this::seedCampaign);
    //    CAMPAIGN_SCENARIOS.forEach(this::seedAssignment);
    //    CAMPAIGN_SCENARIOS.forEach(this::seedDeal);
    //    CAMPAIGN_SCENARIOS.forEach(this::seedClaim);
    LOGGER.warn("==========================================================");
    LOGGER.warn("  DEV SEED — test credentials (h2 profile only)");
    LOGGER.warn("  Admin    mobile=9000000001  password=test1234");
    LOGGER.warn("  Brand    mobile=9100000001  password=test1234");
    LOGGER.warn("  Agency   mobile=9200000001  password=test1234");
    LOGGER.warn("  Mediator mobile=9300000001  password=test1234");
    LOGGER.warn("  Buyer    mobile=9400000001  password=test1234");
    LOGGER.warn("==========================================================");
  }

  private void seedCampaign(final String scenario) {
    final Campaign campaign =
        FileUtils.loadResourceAsObject(
            "/fixtures/seed/" + scenario + "/campaign.json", Campaign.class);
    if (rowExists("campaigns", campaign.getId())) {
      return;
    }
    seedProduct(campaign.getProduct());
    insertCampaign(campaign);
  }

  private void seedProduct(final Product product) {
    if (rowExists("products", product.getId())) {
      return;
    }
    this.jdbcTemplate.update(
        "INSERT INTO products (id, name, image_url, product_link, price_paise)"
            + " VALUES (?, ?, ?, ?, ?)",
        product.getId(),
        product.getName(),
        product.getImageUrl().toString(),
        product.getProductLink().toString(),
        product.getPricePaise());
  }

  private void insertCampaign(final Campaign campaign) {
    final Timestamp now = Timestamp.from(Instant.now());
    this.jdbcTemplate.update(
        "INSERT INTO campaigns (id, title, owner_id, total_slots, product_id, platform, type,"
            + " status, end_date, open_to_all, campaign_price_paise, return_window_days,"
            + " terms_and_conditions, seller_name, created_by, updated_by, created_at,"
            + " updated_at, is_deleted) VALUES"
            + " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        campaign.getId(),
        campaign.getTitle(),
        campaign.getOwnerId(),
        campaign.getTotalSlots(),
        campaign.getProduct().getId(),
        campaign.getPlatform().name(),
        campaign.getType() == null ? null : campaign.getType().name(),
        campaign.getStatus() == null ? null : campaign.getStatus().name(),
        campaign.getEndDate(),
        campaign.isOpenToAll(),
        campaign.getCampaignPricePaise(),
        campaign.getReturnWindowDays(),
        campaign.getTermsAndConditions(),
        campaign.getSellerName(),
        campaign.getCreatedBy(),
        campaign.getUpdatedBy(),
        now,
        now,
        false);
  }

  private void seedAssignment(final String scenario) {
    final CampaignAssignment assignment =
        FileUtils.loadResourceAsObject(
            "/fixtures/seed/" + scenario + "/assignment.json", CampaignAssignment.class);
    if (rowExists("campaign_assignments", assignment.getId())) {
      return;
    }
    insertCampaignSlot(assignment.getCampaignSlot());
    insertCampaignAssignment(assignment);
  }

  private void insertCampaignSlot(final CampaignSlot slot) {
    if (rowExists("campaign_slots", slot.getId())) {
      return;
    }
    final Timestamp now = Timestamp.from(Instant.now());
    this.jdbcTemplate.update(
        "INSERT INTO campaign_slots (id, campaign_id, total_slots, slots_available, created_by,"
            + " updated_by, created_at, updated_at, is_deleted)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
        slot.getId(),
        slot.getCampaignId(),
        slot.getTotalSlots(),
        slot.getSlotsAvailable(),
        slot.getCreatedBy(),
        slot.getUpdatedBy(),
        now,
        now,
        false);
  }

  private void insertCampaignAssignment(final CampaignAssignment assignment) {
    final Timestamp now = Timestamp.from(Instant.now());
    this.jdbcTemplate.update(
        "INSERT INTO campaign_assignments (id, campaign_id, assignor_id, assignee_id, slot_limit,"
            + " adjusted_campaign_price_paise, commission_offered_paise, status, slot_id, created_by,"
            + " updated_by, created_at, updated_at, is_deleted)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        assignment.getId(),
        assignment.getCampaignId(),
        assignment.getAssignorId(),
        assignment.getAssigneeId(),
        assignment.getSlotLimit(),
        assignment.getAdjustedCampaignPricePaise(),
        assignment.getCommissionOfferedPaise(),
        assignment.getStatus() == null ? null : assignment.getStatus().name(),
        assignment.getCampaignSlot().getId(),
        assignment.getCreatedBy(),
        assignment.getUpdatedBy(),
        now,
        now,
        false);
  }

  private void seedDeal(final String scenario) {
    final String path = "/fixtures/seed/" + scenario + "/deal.json";
    if (DevDataSeeder.class.getResource(path) == null) {
      return;
    }
    final DealSeed deal = FileUtils.loadResourceAsObject(path, DealSeed.class);
    if (rowExists("deals", deal.id())) {
      return;
    }
    final Map<String, Object> assignment =
        this.jdbcTemplate.queryForMap(
            "SELECT campaign_id, slot_id FROM campaign_assignments WHERE id = ?",
            deal.assignmentId());
    final Timestamp now = Timestamp.from(Instant.now());
    this.jdbcTemplate.update(
        "INSERT INTO deals (id, owner_id, campaign_id, slot_id, deal_price_paise, created_by,"
            + " updated_by, created_at, updated_at, is_deleted)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        deal.id(),
        deal.ownerId(),
        assignment.get("campaign_id"),
        assignment.get("slot_id"),
        deal.dealPricePaise(),
        deal.createdBy(),
        deal.updatedBy(),
        now,
        now,
        false);
  }

  private void seedClaim(final String scenario) {
    final String path = "/fixtures/seed/" + scenario + "/claim.json";
    if (DevDataSeeder.class.getResource(path) == null) {
      return;
    }
    final Claim claim = FileUtils.loadResourceAsObject(path, Claim.class);
    if (rowExists("claims", claim.getId())) {
      return;
    }
    final Timestamp now = Timestamp.from(Instant.now());
    this.jdbcTemplate.update(
        "INSERT INTO claims (id, campaign_id, deal_id, owner_id, status, platform,"
            + " ecommerce_order_id, amount_paise, product_name, seller_name, order_date,"
            + " account_name, review_url, mediator_verified, score, reviewer_comments,"
            + " created_by, updated_by, created_at, updated_at, is_deleted)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        claim.getId(),
        claim.getCampaignId(),
        claim.getDealId(),
        claim.getOwnerId(),
        claim.getStatus().name(),
        claim.getPlatform().name(),
        claim.getEcommerceOrderId(),
        claim.getAmountPaise(),
        claim.getProductName(),
        claim.getSellerName(),
        claim.getOrderDate(),
        claim.getAccountName(),
        claim.getReviewUrl(),
        claim.getMediatorVerified(),
        claim.getScore(),
        claim.getReviewerComments(),
        claim.getCreatedBy(),
        claim.getUpdatedBy(),
        now,
        now,
        false);
  }

  private void seedConnections() {
    insertConnection(
        UUID.fromString("a0000000-0000-0000-0000-000000000001"),
        BRAND_ID,
        AGENCY_ID,
        ConnectionStatus.CONNECTION_STATUS_ACCEPTED);
    insertConnection(
        UUID.fromString("a0000000-0000-0000-0000-000000000002"),
        MEDIATOR_ID,
        BUYER_ID,
        ConnectionStatus.CONNECTION_STATUS_ACCEPTED);
    insertConnection(
        UUID.fromString("a0000000-0000-0000-0000-000000000003"),
        AGENCY_ID,
        ADMIN_ID,
        ConnectionStatus.CONNECTION_STATUS_REJECTED);
    insertConnection(
        UUID.fromString("a0000000-0000-0000-0000-000000000004"),
        AGENCY_ID,
        MEDIATOR_ID,
        ConnectionStatus.CONNECTION_STATUS_ACCEPTED);
  }

  private void insertConnection(
      final UUID id, final UUID fromUserId, final UUID toUserId, final ConnectionStatus status) {
    if (rowExists("connections", id)) {
      return;
    }
    final Timestamp now = Timestamp.from(Instant.now());
    this.jdbcTemplate.update(
        "INSERT INTO connections (id, from_user_id, to_user_id, status, created_by, updated_by,"
            + " created_at, updated_at, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
        id,
        fromUserId,
        toUserId,
        status.name(),
        fromUserId,
        fromUserId,
        now,
        now,
        false);
  }

  private boolean rowExists(final String table, final UUID id) {
    final Integer count =
        this.jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + table + " WHERE id = ?", Integer.class, id);
    return count != null && count > 0;
  }

  private void seedUser(
      final String name,
      final String mobile,
      final String rawPassword,
      final UserRole role,
      final UUID id) {
    if (rowExists("users", id)) {
      return;
    }
    final Timestamp now = Timestamp.from(Instant.now());
    this.jdbcTemplate.update(
        "INSERT INTO users (id, name, mobile, role, status, created_at, updated_at, is_deleted)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        id,
        name,
        mobile,
        role.name(),
        UserStatus.USER_STATUS_ACTIVE.name(),
        now,
        now,
        false);
    this.jdbcTemplate.update(
        "INSERT INTO user_credentials (id, user_id, password_hash, created_at, updated_at,"
            + " is_deleted) VALUES (?, ?, ?, ?, ?, ?)",
        UUID.randomUUID(),
        id,
        this.passwordService.hashPassword(rawPassword),
        now,
        now,
        false);
  }

  private record DealSeed(
      UUID id,
      UUID assignmentId,
      BigInteger dealPricePaise,
      UUID ownerId,
      UUID createdBy,
      UUID updatedBy) {}
}
