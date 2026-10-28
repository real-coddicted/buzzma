package com.coddicted.buzzma.config;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UserStatus;
import com.coddicted.buzzma.shared.common.PasswordService;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  private static final UUID ADMIN_ID = UUID.fromString("5eed0001-0000-0000-0000-000000000001");
  private static final UUID SUPER_ADMIN_ID =
      UUID.fromString("5eed0002-0000-0000-0000-000000000001");

  private final PasswordService passwordService;
  private final JdbcTemplate jdbcTemplate;
  private final boolean seedInitialUser;
  private final String seedName;
  private final String seedMobile;
  private final String seedPassword;
  private final boolean seedSuperAdmin;
  private final String seedSuperAdminName;
  private final String seedSuperAdminMobile;
  private final String seedSuperAdminPassword;

  public DataSeeder(
      final PasswordService passwordService,
      final JdbcTemplate jdbcTemplate,
      @Value("${app.seed.initial-user:false}") final boolean seedInitialUser,
      @Value("${app.seed.name}") final String seedName,
      @Value("${app.seed.mobile}") final String seedMobile,
      @Value("${app.seed.password}") final String seedPassword,
      @Value("${app.seed.super-admin.enabled:false}") final boolean seedSuperAdmin,
      @Value("${app.seed.super-admin.name}") final String seedSuperAdminName,
      @Value("${app.seed.super-admin.mobile}") final String seedSuperAdminMobile,
      @Value("${app.seed.super-admin.password}") final String seedSuperAdminPassword) {
    this.passwordService = passwordService;
    this.jdbcTemplate = jdbcTemplate;
    this.seedInitialUser = seedInitialUser;
    this.seedName = seedName;
    this.seedMobile = seedMobile;
    this.seedPassword = seedPassword;
    this.seedSuperAdmin = seedSuperAdmin;
    this.seedSuperAdminName = seedSuperAdminName;
    this.seedSuperAdminMobile = seedSuperAdminMobile;
    this.seedSuperAdminPassword = seedSuperAdminPassword;
  }

  @Override
  @Transactional
  public void run(final ApplicationArguments args) {
    LOGGER.warn("==========================================================");
    if (seedInitialUser
        && StringUtils.isNotBlank(seedName)
        && StringUtils.isNotBlank(seedMobile)
        && StringUtils.isNotBlank(seedPassword)) {
      seedUser(seedName, seedMobile, seedPassword, UserRole.ROLE_ADMIN, ADMIN_ID);
      LOGGER.warn("  Data SEED — test credentials created");
    } else {
      LOGGER.warn("No data seed has been initialised");
    }

    if (seedSuperAdmin
        && StringUtils.isNotBlank(seedSuperAdminName)
        && StringUtils.isNotBlank(seedSuperAdminMobile)
        && StringUtils.isNotBlank(seedSuperAdminPassword)) {
      seedUser(
          seedSuperAdminName,
          seedSuperAdminMobile,
          seedSuperAdminPassword,
          UserRole.ROLE_SUPER_ADMIN,
          SUPER_ADMIN_ID);
      LOGGER.warn("  Data SEED — super-admin credentials created");
    } else {
      LOGGER.warn("No super-admin data seed has been initialised");
    }
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
