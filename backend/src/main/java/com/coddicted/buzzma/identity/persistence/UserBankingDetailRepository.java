package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserBankingDetailRepository extends JpaRepository<UserBankingDetail, UUID> {

}
