package com.coddicted.buzzma.shared.constants;

public enum WellKnownSequences {
  CAMPAIGN("campaign_code_seq", "CAMP"),
  DEAL("deal_code_seq", "DEAL"),
  CLAIM("claim_code_seq", "CLAM"),
  USER("user_code_seq", "USER"),
  INVITE("invite_code_seq", "INVT"),
  TICKET("ticket_code_seq", "TIKT");

  private final String sequenceName;
  private final String prefix;

  WellKnownSequences(final String sequenceName, final String prefix) {
    this.sequenceName = sequenceName;
    this.prefix = prefix;
  }

  public String getSequenceName() {
    return this.sequenceName;
  }

  public String getPrefix() {
    return this.prefix;
  }
}
