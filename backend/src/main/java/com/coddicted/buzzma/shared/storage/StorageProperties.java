package com.coddicted.buzzma.shared.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

  private String baseDir = "./storage";

  public String getBaseDir() {
    return baseDir;
  }

  public void setBaseDir(final String baseDir) {
    this.baseDir = baseDir;
  }
}
