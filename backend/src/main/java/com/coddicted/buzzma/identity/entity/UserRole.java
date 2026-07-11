package com.coddicted.buzzma.identity.entity;

public enum UserRole {
  ROLE_BUYER,
  ROLE_MEDIATOR,
  ROLE_AGENCY,
  ROLE_BRAND,
  ROLE_ADMIN;

  public static final class Expr {
    public static final String OR = " or ";
    public static final String AND = " and ";

    public static final String BUYER = "hasRole('BUYER')";
    public static final String MEDIATOR = "hasRole('MEDIATOR')";
    public static final String AGENCY = "hasRole('AGENCY')";
    public static final String BRAND = "hasRole('BRAND')";
    public static final String ADMIN = "hasRole('ADMIN')";
  }
}
