package com.coddicted.buzzma.claim.client;

import com.coddicted.buzzma.shared.score.PayloadItem;
import java.util.List;

public interface ScoreApiClientProxy {
  ExtractedScoredResult score(final String key, final List<PayloadItem> payload);
}
