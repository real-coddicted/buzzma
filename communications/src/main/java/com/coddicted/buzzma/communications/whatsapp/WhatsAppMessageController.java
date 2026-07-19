package com.coddicted.buzzma.communications.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/whatsapp/messages")
public class WhatsAppMessageController {

  private final WhatsAppClient whatsAppClient;

  public WhatsAppMessageController(final WhatsAppClient whatsAppClient) {
    this.whatsAppClient = whatsAppClient;
  }

  @PostMapping
  public ResponseEntity<JsonNode> send(@RequestBody final JsonNode message) {
    if (!(message instanceof ObjectNode objectNode)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    try {
      return ResponseEntity.ok(this.whatsAppClient.sendMessage(objectNode));
    } catch (RestClientResponseException ex) {
      return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAs(JsonNode.class));
    }
  }
}
