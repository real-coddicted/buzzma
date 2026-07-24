package com.coddicted.buzzma.configurator.exception;

public class ConfigEntryConflictException extends RuntimeException {

  public ConfigEntryConflictException(long expectedChangeSeq, long actualChangeSeq) {
    super(
        "Optimistic concurrency conflict: expected change_seq "
            + expectedChangeSeq
            + " but current change_seq is "
            + actualChangeSeq);
  }
}
