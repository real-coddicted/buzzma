package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBankingDetailRepository extends JpaRepository<UserBankingDetail, UUID> {}
