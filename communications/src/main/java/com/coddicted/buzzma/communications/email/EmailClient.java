package com.coddicted.buzzma.communications.email;

import com.coddicted.buzzma.communications.config.EmailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailClient {

  private final JavaMailSender mailSender;
  private final EmailProperties properties;

  public EmailClient(final JavaMailSender mailSender, final EmailProperties properties) {
    this.mailSender = mailSender;
    this.properties = properties;
  }

  public void send(final String to, final String subject, final String body) {
    final SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(this.properties.getFromAddress());
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    this.mailSender.send(message);
  }
}
