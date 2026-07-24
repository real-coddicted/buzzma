package com.coddicted.buzzma.claim.config;

import com.coddicted.buzzma.claim.processor.ChainedScreenshotProcessor;
import com.coddicted.buzzma.claim.processor.ClaimScreenshotProcessor;
import com.coddicted.buzzma.claim.processor.OrderScreenshotProcessor;
import com.coddicted.buzzma.claim.processor.RatingScreenshotProcessor;
import com.coddicted.buzzma.claim.processor.ReturnScreenshotProcessor;
import com.coddicted.buzzma.claim.processor.ReviewScreenshotProcessor;
import com.coddicted.buzzma.claim.scorer.ChainedScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.OrderScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.RatingScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.ReturnScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.ReviewScreenshotScorer;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaimReviewConfig {
  @Bean("ClaimScreenshotProcessor")
  public ClaimScreenshotProcessor claimScreenshotProcessor(
      final OrderScreenshotProcessor orderScreenshotProcessor,
      final RatingScreenshotProcessor ratingScreenshotProcessor,
      final ReviewScreenshotProcessor reviewScreenshotProcessor,
      final ReturnScreenshotProcessor returnScreenshotProcessor) {
    return new ChainedScreenshotProcessor(
        List.of(
            orderScreenshotProcessor,
            ratingScreenshotProcessor,
            reviewScreenshotProcessor,
            returnScreenshotProcessor));
  }

  @Bean("ClaimScreenshotScorer")
  public ClaimScreenshotScorer claimScreenshotScorer(
      final OrderScreenshotScorer orderScreenshotScorer,
      final RatingScreenshotScorer ratingScreenshotScorer,
      final ReviewScreenshotScorer reviewScreenshotScorer,
      final ReturnScreenshotScorer returnScreenshotScorer) {
    return new ChainedScreenshotScorer(
        List.of(
            orderScreenshotScorer,
            ratingScreenshotScorer,
            reviewScreenshotScorer,
            returnScreenshotScorer));
  }
}
