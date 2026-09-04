package com.coddicted.buzzma.campaign.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * One owner-funded reward on a campaign, on top of the campaign discount. A campaign can carry more
 * than one (e.g. cashback and a promo code together). {@code value} is type-dependent: for {@code
 * CASHBACK} it's the amount in paise; for a future code-based type it would be the code.
 */
@Value
@Builder
@Jacksonized
public class Reward {

  RewardType type;

  String value;
}
