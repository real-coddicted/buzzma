package com.coddicted.buzzma.campaign.entity;

import java.net.URL;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * One product offered in exchange on a {@code CAMPAIGN_TYPE_EXCHANGE} campaign, selected from the
 * owner's {@code AgencyExchangeProduct} master list.
 */
@Value
@Builder
@Jacksonized
public class ExchangeProduct {

  String productName;

  URL productImageUrl;
}
