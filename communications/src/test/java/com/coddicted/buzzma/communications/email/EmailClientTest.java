package com.coddicted.buzzma.communications.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.communications.config.EmailProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailClientTest {

  @Test
  void sendsMailWithConfiguredFromAddress() {
    final JavaMailSender mailSender = mock(JavaMailSender.class);
    final EmailProperties properties = new EmailProperties();
    properties.setFromAddress("donotreply@buzzmah.com");
    final EmailClient client = new EmailClient(mailSender, properties);

    client.send("someone@example.com", "Hello", "Body text");

    final ArgumentCaptor<SimpleMailMessage> captor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    final SimpleMailMessage sent = captor.getValue();
    assertEquals("donotreply@buzzmah.com", sent.getFrom());
    assertEquals("someone@example.com", sent.getTo()[0]);
    assertEquals("Hello", sent.getSubject());
    assertEquals("Body text", sent.getText());
  }
}
