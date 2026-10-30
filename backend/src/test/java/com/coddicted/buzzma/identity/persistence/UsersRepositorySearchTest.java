package com.coddicted.buzzma.identity.persistence;

import static com.coddicted.buzzma.identity.persistence.Fixtures.RAVI_AGENCY;
import static com.coddicted.buzzma.identity.persistence.Fixtures.RAVI_BUYER;
import static com.coddicted.buzzma.identity.persistence.Fixtures.RAVI_DELETED_BUYER;
import static com.coddicted.buzzma.identity.persistence.Fixtures.RAVI_MEDIATOR;
import static com.coddicted.buzzma.identity.persistence.Fixtures.SEARCH_TERM;
import static org.assertj.core.api.Assertions.assertThat;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class UsersRepositorySearchTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private UsersRepository usersRepository;

  @BeforeEach
  void setUp() {
    this.usersRepository.saveAll(
        List.of(
            RAVI_BUYER.toBuilder().build(),
            RAVI_MEDIATOR.toBuilder().build(),
            RAVI_AGENCY.toBuilder().build(),
            RAVI_DELETED_BUYER.toBuilder().build()));
  }

  @Test
  void returnsOnlyActiveUsersWithGivenRoles() {
    final List<String> names =
        this.usersRepository
            .searchByNameOrMobileAndRoleIn(
                SEARCH_TERM,
                Set.of(UserRole.ROLE_BUYER, UserRole.ROLE_MEDIATOR),
                PageRequest.of(0, 20))
            .map(BuzzmaUser::getName)
            .getContent();

    assertThat(names).containsExactlyInAnyOrder("Ravi Buyer", "Ravi Mediator");
  }

  @Test
  void returnsNothingWhenNoUserHasGivenRole() {
    assertThat(
            this.usersRepository
                .searchByNameOrMobileAndRoleIn(
                    SEARCH_TERM, Set.of(UserRole.ROLE_BRAND), PageRequest.of(0, 20))
                .getContent())
        .isEmpty();
  }
}
