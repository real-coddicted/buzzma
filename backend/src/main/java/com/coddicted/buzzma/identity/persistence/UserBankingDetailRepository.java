package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBankingDetailRepository extends JpaRepository<UserBankingDetail, UUID> {

  Optional<UserBankingDetail> findByUserIdAndIsDeletedFalse(UUID userId);

  List<UserBankingDetail> findByUserIdInAndIsDeletedFalse(Collection<UUID> userIds);
}
