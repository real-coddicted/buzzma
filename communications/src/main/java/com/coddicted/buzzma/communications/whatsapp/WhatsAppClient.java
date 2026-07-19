package com.coddicted.buzzma.communications.whatsapp;

import com.coddicted.buzzma.communications.config.WhatsAppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WhatsAppClient {

  private final RestClient restClient;
  private final WhatsAppProperties properties;

  public WhatsAppClient(
      final RestClient.Builder restClientBuilder, final WhatsAppProperties properties) {
    this.properties = properties;
    this.restClient = restClientBuilder.baseUrl(properties.getGraphBaseUrl()).build();
  }

  public JsonNode sendMessage(final ObjectNode message) {
    if (!message.has("messaging_product")) {
      message.put("messaging_product", "whatsapp");
    }
    return this.restClient
        .post()
        .uri(
            "/{version}/{phoneNumberId}/messages",
            this.properties.getApiVersion(),
            this.properties.getPhoneNumberId())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + this.properties.getAccessToken())
        .contentType(MediaType.APPLICATION_JSON)
        .body(message)
        .retrieve()
        .body(JsonNode.class);
  }
}
