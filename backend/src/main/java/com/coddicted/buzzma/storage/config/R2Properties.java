package com.coddicted.buzzma.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.r2")
public class R2Properties {

  private boolean enabled;
  private String endpoint;
  private String bucket;
  private String accessKey;
  private String secretKey;
  private String publicUrlBase;
  private int maxUploadAttempts = 5;

  public int getMaxUploadAttempts() {
    return this.maxUploadAttempts;
  }

  public void setMaxUploadAttempts(final int maxUploadAttempts) {
    this.maxUploadAttempts = maxUploadAttempts;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public String getEndpoint() {
    return this.endpoint;
  }

  public void setEndpoint(final String endpoint) {
    this.endpoint = endpoint;
  }

  public String getBucket() {
    return this.bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getAccessKey() {
    return this.accessKey;
  }

  public void setAccessKey(final String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return this.secretKey;
  }

  public void setSecretKey(final String secretKey) {
    this.secretKey = secretKey;
  }

  public String getPublicUrlBase() {
    return this.publicUrlBase;
  }

  public void setPublicUrlBase(final String publicUrlBase) {
    this.publicUrlBase = publicUrlBase;
  }
}
