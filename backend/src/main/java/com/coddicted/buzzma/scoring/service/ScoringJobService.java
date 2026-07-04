package com.coddicted.buzzma.scoring.service;

import com.coddicted.buzzma.scoring.entity.ScoringJob;

public interface ScoringJobService {

  ScoringJob processJob(ScoringJob job);
}
