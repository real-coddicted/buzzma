package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<BuzzmaUser, UUID> {

  Page<BuzzmaUser> findAllByIsDeletedFalse(Pageable pageable);

  boolean existsUserByMobile(String mobile);
}
