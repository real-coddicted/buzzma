package com.coddicted.buzzma.config;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UserStatus;
import com.coddicted.buzzma.identity.persistence.UserCredentialRepository;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.common.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local"})
public class DevDataSeeder implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DevDataSeeder.class);

  private final UsersRepository usersRepository;
  private final UserCredentialRepository userCredentialRepository;
  private final PasswordService passwordService;

  public DevDataSeeder(
      final UsersRepository usersRepository,
      final UserCredentialRepository userCredentialRepository,
      final PasswordService passwordService) {
    this.usersRepository = usersRepository;
    this.userCredentialRepository = userCredentialRepository;
    this.passwordService = passwordService;
  }

  @Override
  @Transactional
  public void run(final ApplicationArguments args) {
    seedUser("Test Admin", "9000000001", "admin123", UserRole.ROLE_ADMIN);
    seedUser("Test Buyer", "9000000002", "buyer123", UserRole.ROLE_BUYER);
    seedUser("Test Agency", "9000000003", "agency123", UserRole.ROLE_AGENCY);
    seedUser("Test Brand", "9000000004", "brand123", UserRole.ROLE_BRAND);
    LOGGER.warn("==========================================================");
    LOGGER.warn("  DEV SEED — test credentials (h2 profile only)");
    LOGGER.warn("  Admin   mobile=9000000001  password=admin123");
    LOGGER.warn("  Buyer   mobile=9000000002  password=buyer123");
    LOGGER.warn("==========================================================");
  }

  private void seedUser(
      final String name, final String mobile, final String rawPassword, final UserRole role) {
    if (this.usersRepository.existsUserByMobileAndIsDeletedFalse(mobile)) {
      return;
    }
    final BuzzmaUser user =
        BuzzmaUser.builder()
            .name(name)
            .mobile(mobile)
            .role(role)
            .status(UserStatus.USER_STATUS_ACTIVE)
            .isDeleted(false)
            .build();
    final BuzzmaUser savedUser = this.usersRepository.save(user);

    final UserCredential credential =
        UserCredential.builder()
            .userId(savedUser.getId())
            .passwordHash(this.passwordService.hashPassword(rawPassword))
            .isDeleted(false)
            .build();
    this.userCredentialRepository.save(credential);
  }
}
