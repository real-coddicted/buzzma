package com.coddicted.buzzma.mediator.service;

import java.util.List;

public interface LineageService {

  List<String> listMediatorCodesForAgency(String agencyCode);

  String getAgencyCodeForMediatorCode(String mediatorCode);

  boolean isAgencyActive(String agencyCode);

  boolean isMediatorActive(String mediatorCode);

  void clearCache();
}
