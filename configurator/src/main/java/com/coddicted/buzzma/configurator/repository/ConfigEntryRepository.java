package com.coddicted.buzzma.configurator.repository;

import com.coddicted.buzzma.configurator.entity.ConfigEntry;
import com.coddicted.buzzma.configurator.enums.EntryStatusEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConfigEntryRepository extends JpaRepository<ConfigEntry, UUID> {

  List<ConfigEntry> findByNamespaceAndEnvironmentAndStatus(
      @Param("namespace") String namespace,
      @Param("environment") String environment,
      @Param("status") EntryStatusEnum status);

  Optional<ConfigEntry> findByNamespaceAndEnvironmentAndKey(
      String namespace, String environment, String key);

  boolean existsByNamespaceAndEnvironmentAndKey(String namespace, String environment, String key);

  @Query(
      "SELECT e FROM ConfigEntry e"
          + " WHERE e.namespace = :namespace AND e.environment = :environment"
          + " AND e.changeSeq > :sinceChangeSeq"
          + " ORDER BY e.changeSeq ASC")
  List<ConfigEntry> findDelta(
      @Param("namespace") String namespace,
      @Param("environment") String environment,
      @Param("sinceChangeSeq") long sinceChangeSeq);

  @Query(
      value =
          "SELECT * FROM config_entries"
              + " WHERE namespace = :namespace AND environment = :environment"
              + " AND status <> 'DELETED'"
              + " AND (LOWER(key) LIKE LOWER(CONCAT('%', :search, '%'))"
              + "   OR LOWER(COALESCE(description, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
      countQuery =
          "SELECT COUNT(*) FROM config_entries"
              + " WHERE namespace = :namespace AND environment = :environment"
              + " AND status <> 'DELETED'"
              + " AND (LOWER(key) LIKE LOWER(CONCAT('%', :search, '%'))"
              + "   OR LOWER(COALESCE(description, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
      nativeQuery = true)
  Page<ConfigEntry> search(
      @Param("namespace") String namespace,
      @Param("environment") String environment,
      @Param("search") String search,
      Pageable pageable);
}
