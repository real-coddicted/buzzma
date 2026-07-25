package com.coddicted.buzzma.configurator.repository;

import com.coddicted.buzzma.configurator.entity.ConfigEntryHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigEntryHistoryRepository extends JpaRepository<ConfigEntryHistory, Long> {

  List<ConfigEntryHistory> findByEntryIdOrderByChangedAtDesc(UUID entryId);
}
