package com.coddicted.buzzma.claim.model;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.claim.entity.Claim;

public record ClaimWithDeal(Claim claim, Deal deal) {}
