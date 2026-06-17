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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DataSeeder implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  private static final UUID ADMIN_ID = UUID.fromString("5eed0001-0000-0000-0000-000000000001");

  private final PasswordService passwordService;
  private final JdbcTemplate jdbcTemplate;
  private final boolean seedInitialUser;

  public DataSeeder(
      final PasswordService passwordService,
      final JdbcTemplate jdbcTemplate,
      @Value("${app.seed.initial-user:false}") final boolean seedInitialUser) {
    this.passwordService = passwordService;
    this.jdbcTemplate = jdbcTemplate;
    this.seedInitialUser = seedInitialUser;
  }

  @Override
  @Transactional
  public void run(final ApplicationArguments args) {
    LOGGER.warn("==========================================================");
    if (!seedInitialUser) {
      LOGGER.warn("No data seed has been initialised");
      return;
    }
    seedUser("Test Admin 1", "9000000001", "test1234", UserRole.ROLE_ADMIN, ADMIN_ID);


    LOGGER.warn("  Data SEED — test credentials created");
    LOGGER.warn("==========================================================");
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

}
