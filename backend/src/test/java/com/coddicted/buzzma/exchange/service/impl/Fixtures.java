package com.coddicted.buzzma.exchange.service.impl;

import com.coddicted.buzzma.exchange.entity.AgencyExchangeProduct;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;

public final class Fixtures {

  static final UUID AGENCY_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

  static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  static final UUID MISSING_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

  static final AgencyExchangeProduct PRODUCT_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/exchange/exchange-product-1.json", AgencyExchangeProduct.class);

  static final AgencyExchangeProduct PRODUCT_2 =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/exchange/exchange-product-2.json", AgencyExchangeProduct.class);

  static final AgencyExchangeProduct NEW_PRODUCT =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/exchange/exchange-product-new.json", AgencyExchangeProduct.class);

  private Fixtures() {}
}
