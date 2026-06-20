package com.coddicted.buzzma.extraction.service;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;

public interface ExtractionJobService {

  ExtractionJob processJob(ExtractionJob job);
}
