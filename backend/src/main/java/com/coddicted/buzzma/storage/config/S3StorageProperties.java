package com.coddicted.buzzma.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage.s3")
public class S3StorageProperties {

  private String endpoint;
  private String bucket;
  private String accessKey;
  private String secretKey;
  private String region = "garage";

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

  public String getRegion() {
    return this.region;
  }

  public void setRegion(final String region) {
    this.region = region;
  }
}
