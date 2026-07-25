package com.coddicted.buzzma.configurator.exception;

import java.util.UUID;

public class ConfigEntryNotFoundException extends RuntimeException {

  public ConfigEntryNotFoundException(UUID id) {
    super("Config entry not found: " + id);
  }

  public ConfigEntryNotFoundException(String namespace, String environment, String key) {
    super("Config entry not found: " + namespace + "/" + environment + "/" + key);
  }
}
