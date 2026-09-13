package com.coddicted.buzzma.claim.step;

/** One weighted input into a step's overall score, e.g. ("rating", 1.0). */
public record ScoringCriterion(String name, double weight) {}
