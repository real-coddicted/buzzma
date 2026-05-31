package com.coddicted.buzzma.identity.entity;

public enum UserStatus {
  USER_STATUS_ACTIVE,
  USER_STATUS_SUSPENDED, // suspicious activity or violation of terms of service, payment failure,
  // etc.
  USER_STATUS_LOCKED // associated with incorrect password attempts
}
