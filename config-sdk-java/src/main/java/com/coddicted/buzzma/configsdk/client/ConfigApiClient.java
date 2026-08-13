package com.coddicted.buzzma.configsdk.client;

import org.springframework.web.client.RestClient;

/**
 * Wraps the two read endpoints the SDK uses (bulk fetch, delta poll). Owns retry/timeout logic only
 * — no caching, no business logic. SDKs never call the single-key or admin endpoints (design doc
 * §6); those exist for the admin UI.
 *
 * <p>Timeouts are configured on the injected {@link RestClient}'s request factory (see {@code
 * ConfigSdkAutoConfiguration}), not here — callers (bootstrap sequence, poller) decide how to react
 * to a timeout or failure, this class just lets it propagate as an unchecked {@link
 * org.springframework.web.client.RestClientException}.
 */
public class ConfigApiClient {

  private final RestClient restClient;

  public ConfigApiClient(final RestClient restClient) {
    this.restClient = restClient;
  }

  public BulkFetchResult bulkFetch(final String namespace, final String environment) {
    return restClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/v1/configs")
                    .queryParam("namespace", namespace)
                    .queryParam("environment", environment)
                    .build())
        .retrieve()
        .body(BulkFetchResult.class);
  }

  public DeltaPollResult deltaPoll(
      final String namespace, final String environment, final long sinceChangeSeq) {
    return restClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/v1/configs/delta")
                    .queryParam("namespace", namespace)
                    .queryParam("environment", environment)
                    .queryParam("sinceChangeSeq", sinceChangeSeq)
                    .build())
        .retrieve()
        .body(DeltaPollResult.class);
  }
}
