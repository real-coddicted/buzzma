package com.coddicted.buzzma.shared.turnstile;

import com.coddicted.buzzma.shared.exception.ForbiddenException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TurnstileClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(TurnstileClient.class);

  private final TurnstileProperties properties;
  private final RestClient restClient;

  public TurnstileClient(final RestClient.Builder builder, final TurnstileProperties properties) {
    this.properties = properties;
    this.restClient = builder.build();
    LOGGER.info(
        "TurnstileClient initialized: enabled={}, secretKeyPresent={}",
        properties.isEnabled(),
        StringUtils.hasText(properties.getSecretKey()));
  }

  public void verify(final String token) {
    if (!this.properties.isEnabled()) {
      LOGGER.debug("Turnstile verification disabled; skipping captcha check");
      return;
    }

    if (!StringUtils.hasText(token)) {
      LOGGER.warn("Captcha verification failed: missing token");
      throw new ForbiddenException("Captcha verification failed");
    }

    final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("secret", this.properties.getSecretKey());
    form.add("response", token);

    final Map<?, ?> response;
    try {
      response =
          this.restClient
              .post()
              .uri(this.properties.getVerifyUrl())
              .body(form)
              .retrieve()
              .body(Map.class);
    } catch (RestClientException e) {
      LOGGER.warn("Turnstile siteverify call failed: {}", e.getMessage());
      throw new ForbiddenException("Captcha verification failed");
    }

    if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
      LOGGER.warn(
          "Captcha verification rejected: {}",
          response == null ? "null response" : response.get("error-codes"));
      throw new ForbiddenException("Captcha verification failed");
    }

    LOGGER.debug("Captcha verification succeeded");
  }
}
