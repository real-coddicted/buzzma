package com.coddicted.buzzma.campaign.step;

/**
 * One data point this step collects. {@code userProvided} and {@code aiExtracted} are independent —
 * a field can be either, or both when the buyer supplies a value and AI extracts the same field
 * from the screenshot so it can be verified against what the buyer claimed (e.g. {@code
 * accountName}, {@code reviewUrl}).
 */
public record StepField(String name, boolean userProvided, boolean aiExtracted) {}
