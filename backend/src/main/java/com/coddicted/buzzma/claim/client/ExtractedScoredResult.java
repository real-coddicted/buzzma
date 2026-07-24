package com.coddicted.buzzma.claim.client;

import com.coddicted.buzzma.extraction.entity.ScoredValue;
import java.util.Map;

public record ExtractedScoredResult(Map<String, ScoredValue> extractedResult, int overallScore) {}
