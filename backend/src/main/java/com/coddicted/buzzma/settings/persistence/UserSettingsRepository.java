package com.coddicted.buzzma.settings.persistence;

import com.coddicted.buzzma.settings.entity.UserSettings;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, UUID> {
  Optional<UserSettings> findByUserIdAndIsDeletedFalse(UUID userId);
}
