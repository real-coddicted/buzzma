package com.coddicted.buzzma.identity.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.coddicted.buzzma.identity.entity.UsersEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<UsersEntity, UUID> {

  Page<UsersEntity> findAllByIsDeletedFalse(Pageable pageable);

  Optional<UsersEntity> findByMobileAndIsDeletedFalse(String mobile);

  Optional<UsersEntity> findByUsernameAndIsDeletedFalse(String username);

  Optional<UsersEntity> findByEmailAndIsDeletedFalse(String email);

  Optional<UsersEntity> findByMediatorCodeAndIsDeletedFalse(String mediatorCode);

  List<UsersEntity> findAllByParentCodeAndIsDeletedFalse(String parentCode);

  long countByParentCodeAndIsDeletedFalse(String parentCode);

  Page<UsersEntity> findAllByParentCodeAndIsVerifiedByMediatorAndIsDeletedFalse(
      String parentCode, Boolean isVerifiedByMediator, Pageable pageable);

  long countByParentCodeAndIsVerifiedByMediatorFalseAndIsDeletedFalse(String parentCode);
}
