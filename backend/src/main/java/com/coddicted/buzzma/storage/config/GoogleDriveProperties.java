package com.coddicted.buzzma.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.google-drive")
public class GoogleDriveProperties {

  private boolean enabled;
  private String credentialsPath;
  private String rootFolderId;

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public String getCredentialsPath() {
    return this.credentialsPath;
  }

  public void setCredentialsPath(final String credentialsPath) {
    this.credentialsPath = credentialsPath;
  }

  public String getRootFolderId() {
    return this.rootFolderId;
  }

  public void setRootFolderId(final String rootFolderId) {
    this.rootFolderId = rootFolderId;
  }
}
