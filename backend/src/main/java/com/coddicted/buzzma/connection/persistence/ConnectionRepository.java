package com.coddicted.buzzma.connection.persistence;

import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.model.ConnectionSummary;
import com.coddicted.buzzma.connection.model.ConnectionView;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, UUID> {

  Optional<Connection> findByFromUserIdAndToUserIdAndIsDeletedFalse(UUID fromUserId, UUID toUserId);

  @Query(
      """
      SELECT new com.coddicted.buzzma.connection.model.ConnectionView(c, fu.name, tu.name)
      FROM Connection c
      JOIN BuzzmaUser fu ON fu.id = c.fromUserId
      JOIN BuzzmaUser tu ON tu.id = c.toUserId
      WHERE c.fromUserId = :fromUserId AND c.isDeleted = false
      """)
  Set<ConnectionView> findViewsByFromUserId(@Param("fromUserId") UUID fromUserId);

  @Query(
      """
      SELECT new com.coddicted.buzzma.connection.model.ConnectionView(c, fu.name, tu.name)
      FROM Connection c
      JOIN BuzzmaUser fu ON fu.id = c.fromUserId
      JOIN BuzzmaUser tu ON tu.id = c.toUserId
      WHERE c.fromUserId = :fromUserId AND c.status = :status AND c.isDeleted = false
      """)
  Set<ConnectionView> findViewsByFromUserIdAndStatus(
      @Param("fromUserId") UUID fromUserId, @Param("status") ConnectionStatus status);

  @Query(
      """
      SELECT new com.coddicted.buzzma.connection.model.ConnectionSummary(
        COUNT(CASE WHEN c.status = ConnectionStatus.CONNECTION_STATUS_ACCEPTED THEN 1 END),
        COUNT(CASE WHEN c.status = ConnectionStatus.CONNECTION_STATUS_REQUESTED THEN 1 END),
        COUNT(CASE WHEN c.status = ConnectionStatus.CONNECTION_STATUS_REJECTED THEN 1 END))
      FROM Connection c
      WHERE c.fromUserId = :fromUserId AND c.isDeleted = false
      """)
  ConnectionSummary findSummaryByFromUserId(@Param("fromUserId") UUID fromUserId);

  @Query(
      """
      SELECT new com.coddicted.buzzma.connection.model.ConnectionView(c, fu.name, tu.name)
      FROM Connection c
      JOIN BuzzmaUser fu ON fu.id = c.fromUserId
      JOIN BuzzmaUser tu ON tu.id = c.toUserId
      WHERE c.toUserId = :toUserId AND c.isDeleted = false
      """)
  Set<ConnectionView> findViewsByToUserId(@Param("toUserId") UUID toUserId);

  @Query(
      """
      SELECT new com.coddicted.buzzma.connection.model.ConnectionView(c, fu.name, tu.name)
      FROM Connection c
      JOIN BuzzmaUser fu ON fu.id = c.fromUserId
      JOIN BuzzmaUser tu ON tu.id = c.toUserId
      WHERE c.toUserId = :toUserId AND c.status = :status AND c.isDeleted = false
      """)
  Set<ConnectionView> findViewsByToUserIdAndStatus(
      @Param("toUserId") UUID toUserId, @Param("status") ConnectionStatus status);

  @Query(
      """
      SELECT new com.coddicted.buzzma.connection.model.ConnectionSummary(
        COUNT(CASE WHEN c.status = ConnectionStatus.CONNECTION_STATUS_ACCEPTED THEN 1 END),
        COUNT(CASE WHEN c.status = ConnectionStatus.CONNECTION_STATUS_REQUESTED THEN 1 END),
        COUNT(CASE WHEN c.status = ConnectionStatus.CONNECTION_STATUS_REJECTED THEN 1 END))
      FROM Connection c
      WHERE c.toUserId = :toUserId AND c.isDeleted = false
      """)
  ConnectionSummary findSummaryByToUserId(@Param("toUserId") UUID toUserId);

  Optional<Connection> findByToUserIdAndStatusAndIsDeletedFalse(
      UUID toUserId, ConnectionStatus status);

  boolean existsByFromUserIdAndToUserIdAndStatusAndIsDeletedFalse(
      UUID fromUserId, UUID toUserId, ConnectionStatus status);
}
