package com.coddicted.buzzma.shared.exception;

public class PasswordMatchException extends RuntimeException {
  public PasswordMatchException(String message) {
    super(message);
  }
}
