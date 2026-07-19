package com.coddicted.buzzma.communications.email;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email/messages")
public class EmailMessageController {

  private final EmailClient emailClient;

  public EmailMessageController(final EmailClient emailClient) {
    this.emailClient = emailClient;
  }

  @PostMapping
  public ResponseEntity<Void> send(@RequestBody final EmailSendRequest request) {
    try {
      this.emailClient.send(request.getTo(), request.getSubject(), request.getBody());
      return ResponseEntity.ok().build();
    } catch (MailException ex) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
  }
}
