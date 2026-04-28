package com.coddicted.buzzma.mediator.api;

import java.util.UUID;

public interface MediatorAdminPort {

  void requestBrandConnection(
      UUID brandUserId, String brandCode, String agencyCode, String agencyName, UUID actorUserId);
}
