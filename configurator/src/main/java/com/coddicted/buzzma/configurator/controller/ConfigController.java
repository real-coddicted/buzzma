package com.coddicted.buzzma.configurator.controller;

import com.coddicted.buzzma.configurator.dto.BulkFetchResponse;
import com.coddicted.buzzma.configurator.dto.ConfigEntryResponse;
import com.coddicted.buzzma.configurator.dto.CreateConfigRequest;
import com.coddicted.buzzma.configurator.dto.DeltaPollResponse;
import com.coddicted.buzzma.configurator.dto.HistoryEntryResponse;
import com.coddicted.buzzma.configurator.dto.PagedConfigResponse;
import com.coddicted.buzzma.configurator.dto.UpdateConfigRequest;
import com.coddicted.buzzma.configurator.service.ConfigService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/configs")
public class ConfigController {

  private final ConfigService service;

  public ConfigController(final ConfigService service) {
    this.service = service;
  }

  // ── Read endpoints (SDK path) ──────────────────────────────────────────────

  /** Bulk fetch — all active entries for a namespace+environment. Used by SDK on startup. */
  @GetMapping(params = {"namespace", "environment", "!search"})
  public BulkFetchResponse bulkFetch(
      @RequestParam final String namespace, @RequestParam final String environment) {
    return service.bulkFetch(namespace, environment);
  }

  /**
   * Delta poll — entries changed since a given change_seq, including soft-deleted ones so the SDK
   * knows to drop them from its local cache.
   */
  @GetMapping("/delta")
  public DeltaPollResponse deltaPoll(
      @RequestParam final String namespace,
      @RequestParam final String environment,
      @RequestParam final long sinceChangeSeq) {
    return service.deltaPoll(namespace, environment, sinceChangeSeq);
  }

  /** Single-key read. SDKs never use this — exists for admin UI and debugging only. */
  @GetMapping("/{namespace}/{environment}/{key}")
  public ConfigEntryResponse getSingleKey(
      @PathVariable final String namespace,
      @PathVariable final String environment,
      @PathVariable final String key) {
    return service.getSingleKey(namespace, environment, key);
  }

  // ── Admin endpoints ────────────────────────────────────────────────────────

  /** Paginated search across keys and descriptions. Used by admin UI. */
  @GetMapping(params = {"namespace", "environment", "search"})
  public PagedConfigResponse search(
      @RequestParam final String namespace,
      @RequestParam final String environment,
      @RequestParam final String search,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    return service.search(namespace, environment, search, page, size);
  }

  /** Create a new config entry. updated_by is derived from the authenticated caller. */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ConfigEntryResponse create(
      final Authentication authentication, @Valid @RequestBody final CreateConfigRequest request) {
    return service.create(request, authentication.getName());
  }

  /**
   * Update value with optimistic concurrency. Body must include expected_change_seq — returns 409
   * if the row changed since the caller last read it.
   */
  @PutMapping("/{id}")
  public ConfigEntryResponse update(
      final Authentication authentication,
      @PathVariable final UUID id,
      @Valid @RequestBody final UpdateConfigRequest request) {
    return service.update(id, request, authentication.getName());
  }

  /** Soft delete — sets status to 'deleted', never removes the row. */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(final Authentication authentication, @PathVariable final UUID id) {
    service.softDelete(id, authentication.getName());
  }

  /** Full audit trail for a single config entry, newest first. */
  @GetMapping("/{id}/history")
  public List<HistoryEntryResponse> getHistory(@PathVariable final UUID id) {
    return service.getHistory(id);
  }
}
