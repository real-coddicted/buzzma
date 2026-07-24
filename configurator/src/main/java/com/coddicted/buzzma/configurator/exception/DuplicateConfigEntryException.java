package com.coddicted.buzzma.configurator.exception;

public class DuplicateConfigEntryException extends RuntimeException {

  public DuplicateConfigEntryException(String namespace, String environment, String key) {
    super("Config entry already exists: " + namespace + "/" + environment + "/" + key);
  }
}
