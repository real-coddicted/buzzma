package com.coddicted.buzzma.shared.common;

import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class BaseCrudService {

  protected <T> T mustFind(JpaRepository<T, UUID> repository, UUID id, String entityName) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(entityName + " not found: " + id));
  }
}
