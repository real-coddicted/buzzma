package com.coddicted.buzzma.scoring.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ScoringConfig {

  @Bean(name = "scoringTaskExecutor")
  public ThreadPoolTaskExecutor scoringTaskExecutor(
      @Value("${app.scoring.scheduler.thread-pool-size:5}") final int poolSize) {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(poolSize);
    executor.setMaxPoolSize(poolSize);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("scoring-");
    executor.initialize();
    return executor;
  }
}
