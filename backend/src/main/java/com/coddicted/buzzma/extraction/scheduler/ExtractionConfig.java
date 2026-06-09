package com.coddicted.buzzma.extraction.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExtractionConfig {

  @Bean(name = "extractionTaskExecutor")
  public ThreadPoolTaskExecutor extractionTaskExecutor(
      @Value("${app.extraction.scheduler.thread-pool-size:5}") final int poolSize) {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(poolSize);
    executor.setMaxPoolSize(poolSize);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("extraction-");
    executor.initialize();
    return executor;
  }
}
