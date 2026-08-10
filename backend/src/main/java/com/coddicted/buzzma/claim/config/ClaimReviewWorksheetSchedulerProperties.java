package com.coddicted.buzzma.claim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.claim-review.worksheet.scheduler")
public class ClaimReviewWorksheetSchedulerProperties {

  private int batchSize = 50;
  private int maxRetries = 3;
  private int staleThresholdMinutes = 30;

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(final int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public int getStaleThresholdMinutes() {
    return staleThresholdMinutes;
  }

  public void setStaleThresholdMinutes(final int staleThresholdMinutes) {
    this.staleThresholdMinutes = staleThresholdMinutes;
  }
}
