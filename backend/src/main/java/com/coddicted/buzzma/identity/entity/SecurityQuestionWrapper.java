package com.coddicted.buzzma.identity.entity;

import java.util.UUID;

public interface SecurityQuestionWrapper {

  UUID getUserId();

  UUID getQuestionId();

  String getQuestion();
}
