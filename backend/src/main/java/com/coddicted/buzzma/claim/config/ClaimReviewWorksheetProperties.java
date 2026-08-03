package com.coddicted.buzzma.claim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.claim-review.worksheet")
public class ClaimReviewWorksheetProperties {

  private long maxFileSizeBytes = 5L * 1024 * 1024;
  private int batchSize = 100;

  public long getMaxFileSizeBytes() {
    return maxFileSizeBytes;
  }

  public void setMaxFileSizeBytes(final long maxFileSizeBytes) {
    this.maxFileSizeBytes = maxFileSizeBytes;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }
}
