package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {
}
