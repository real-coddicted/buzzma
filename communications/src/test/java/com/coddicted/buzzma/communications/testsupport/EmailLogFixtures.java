package com.coddicted.buzzma.communications.testsupport;

import com.coddicted.buzzma.communications.email.model.EmailCommunicationLog;
import com.coddicted.buzzma.communications.util.FileUtils;
import java.util.UUID;

public final class EmailLogFixtures {

  public static final String TO_ADDRESS = "someone@example.com";
  public static final String FROM_ADDRESS = "donotreply@buzzmah.com";
  public static final String SUBJECT = "Your OTP code";
  public static final String BODY = "Your code is 123456";

  public static final UUID REQUEST_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  public static final UUID REQUESTED_BY = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

  public static final EmailCommunicationLog PENDING_LOG =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/email/pending-log.json", EmailCommunicationLog.class);

  public static final EmailCommunicationLog SENT_LOG =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/email/sent-log.json", EmailCommunicationLog.class);

  public static final EmailCommunicationLog FAILED_LOG =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/email/failed-log.json", EmailCommunicationLog.class);

  private EmailLogFixtures() {}
}
