package com.coddicted.buzzma.identity.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<BuzzmaUser, UUID> {

  Page<BuzzmaUser> findAllByIsDeletedFalse(Pageable pageable);

  boolean existsUserByMobile(String mobile);

  Optional<BuzzmaUser> findByUsernameAndIsDeletedFalse(String username);

  Optional<BuzzmaUser> findByEmailAndIsDeletedFalse(String email);

  Optional<BuzzmaUser> findByMediatorCodeAndIsDeletedFalse(String mediatorCode);

  List<BuzzmaUser> findAllByParentCodeAndIsDeletedFalse(String parentCode);

  long countByParentCodeAndIsDeletedFalse(String parentCode);

  Page<BuzzmaUser> findAllByParentCodeAndIsVerifiedByMediatorAndIsDeletedFalse(
      String parentCode, Boolean isVerifiedByMediator, Pageable pageable);

  long countByParentCodeAndIsVerifiedByMediatorFalseAndIsDeletedFalse(String parentCode);
}
