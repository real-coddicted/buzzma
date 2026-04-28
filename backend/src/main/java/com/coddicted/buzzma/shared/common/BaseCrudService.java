package com.coddicted.buzzma.shared.common;

import com.coddicted.buzzma.shared.exception.ApiException;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

public abstract class BaseCrudService {

    protected <T> T mustFind(JpaRepository<T, UUID> repository, UUID id, String entityName) {
        return repository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ApiException(
                                        HttpStatus.NOT_FOUND, "NOT_FOUND", entityName + " not found: " + id));
    }
}