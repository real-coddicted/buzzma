package com.coddicted.buzzma.mediator.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediatorProfilesRepository extends JpaRepository<MediatorProfilesEntity, UUID> {

  Page<MediatorProfilesEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<MediatorProfilesEntity> findByMediatorCode(String mediatorCode);

  List<MediatorProfilesEntity> findAllByParentAgencyCodeAndIsDeletedFalse(String parentAgencyCode);
}
