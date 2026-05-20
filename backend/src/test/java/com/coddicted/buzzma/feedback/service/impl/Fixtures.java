package com.coddicted.buzzma.feedback.service.impl;

import com.coddicted.buzzma.feedback.entity.Feedback;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;

public final class Fixtures {

  static final UUID FEEDBACK_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

  static final UUID USER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

  static final UUID REQUESTER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

  static final Feedback FEEDBACK_1 =
      FileUtils.loadResourceAsObject("/fixtures/output/feedback/feedback-1.json", Feedback.class);

  static final Feedback FEEDBACK_2 =
      FileUtils.loadResourceAsObject("/fixtures/output/feedback/feedback-2.json", Feedback.class);

  static final Feedback FEEDBACK_3 =
      FileUtils.loadResourceAsObject("/fixtures/output/feedback/feedback-3.json", Feedback.class);

  private Fixtures() {}
}
