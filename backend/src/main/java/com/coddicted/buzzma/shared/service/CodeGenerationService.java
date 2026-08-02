package com.coddicted.buzzma.shared.service;

import com.coddicted.buzzma.shared.constants.WellKnownSequences;

public interface CodeGenerationService {
  String generateCodeFromSequence(WellKnownSequences sequence);
}
