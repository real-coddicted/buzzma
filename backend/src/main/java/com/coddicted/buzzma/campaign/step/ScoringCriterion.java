package com.coddicted.buzzma.campaign.step;

/** One weighted input into a step's overall score, e.g. ("rating", 1.0). */
public record ScoringCriterion(String name, double weight) {}
