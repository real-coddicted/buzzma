package com.coddicted.buzzma.identity.template;

import com.coddicted.buzzma.shared.util.FileUtils;
import org.springframework.stereotype.Component;

@Component
public class OtpEmailTemplate {

  private static final String SUBJECT = "Your Buzzma verification code";
  private static final String TEMPLATE_PATH = "/templates/email/otp-verification.txt";
  private static final String OTP_PLACEHOLDER = "{{otp}}";

  private final String template;

  public OtpEmailTemplate() {
    this.template = FileUtils.loadResourceAsString(TEMPLATE_PATH);
  }

  public String subject() {
    return SUBJECT;
  }

  public String render(final String otp) {
    return this.template.replace(OTP_PLACEHOLDER, otp);
  }
}
