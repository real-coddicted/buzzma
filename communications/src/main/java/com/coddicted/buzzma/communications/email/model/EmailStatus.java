package com.coddicted.buzzma.communications.email.model;

public enum EmailStatus {
  EMAIL_STATUS_PENDING("pending"),
  EMAIL_STATUS_SENT("sent"),
  EMAIL_STATUS_FAILED("failed");

  private final String value;

  EmailStatus(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
