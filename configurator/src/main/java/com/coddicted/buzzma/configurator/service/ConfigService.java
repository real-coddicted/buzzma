package com.coddicted.buzzma.configurator.service;

import com.coddicted.buzzma.configurator.config.ConfiguratorProperties;
import com.coddicted.buzzma.configurator.dto.BulkFetchResponse;
import com.coddicted.buzzma.configurator.dto.ConfigEntryResponse;
import com.coddicted.buzzma.configurator.dto.CreateConfigRequest;
import com.coddicted.buzzma.configurator.dto.DeltaPollResponse;
import com.coddicted.buzzma.configurator.dto.HistoryEntryResponse;
import com.coddicted.buzzma.configurator.dto.PagedConfigResponse;
import com.coddicted.buzzma.configurator.dto.UpdateConfigRequest;
import com.coddicted.buzzma.configurator.entity.ConfigEntry;
import com.coddicted.buzzma.configurator.entity.ConfigEntryHistory;
import com.coddicted.buzzma.configurator.enums.EntryStatusEnum;
import com.coddicted.buzzma.configurator.exception.ConfigEntryConflictException;
import com.coddicted.buzzma.configurator.exception.ConfigEntryNotFoundException;
import com.coddicted.buzzma.configurator.exception.DuplicateConfigEntryException;
import com.coddicted.buzzma.configurator.repository.ConfigEntryHistoryRepository;
import com.coddicted.buzzma.configurator.repository.ConfigEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConfigService {

  private final ConfigEntryRepository entryRepo;
  private final ConfigEntryHistoryRepository historyRepo;
  private final ConfiguratorProperties props;

  @PersistenceContext private EntityManager entityManager;

  // In-process cache for the bulk-fetch endpoint. Collapses thundering-herd
  // on fleet restart — all instances bulk-fetching within a few seconds share
  // one DB read instead of N.
  private final Map<String, CachedBulkFetch> bulkFetchCache = new ConcurrentHashMap<>();

  private record CachedBulkFetch(BulkFetchResponse response, long expiresAtMs) {}

  public ConfigService(
      final ConfigEntryRepository entryRepo,
      final ConfigEntryHistoryRepository historyRepo,
      final ConfiguratorProperties props) {
    this.entryRepo = entryRepo;
    this.historyRepo = historyRepo;
    this.props = props;
  }

  public BulkFetchResponse bulkFetch(final String namespace, final String environment) {
    final String cacheKey = namespace + ":" + environment;
    final CachedBulkFetch cached = bulkFetchCache.get(cacheKey);
    if (cached != null && System.currentTimeMillis() < cached.expiresAtMs()) {
      return cached.response();
    }

    final List<ConfigEntry> entries =
        entryRepo.findByNamespaceAndEnvironmentAndStatus(
            namespace, environment, EntryStatusEnum.ACTIVE);
    final List<ConfigEntryResponse> items = entries.stream().map(this::toResponse).toList();
    final long snapshotChangeSeq =
        items.stream().mapToLong(ConfigEntryResponse::getChangeSeq).max().orElse(0L);

    final BulkFetchResponse response =
        BulkFetchResponse.builder()
            .namespace(namespace)
            .environment(environment)
            .snapshotChangeSeq(snapshotChangeSeq)
            .items(items)
            .build();

    final long ttlMs = props.getBulkFetchCacheTtlSeconds() * 1000L;
    bulkFetchCache.put(cacheKey, new CachedBulkFetch(response, System.currentTimeMillis() + ttlMs));
    return response;
  }

  public DeltaPollResponse deltaPoll(
      final String namespace, final String environment, final long sinceChangeSeq) {
    final List<ConfigEntry> delta = entryRepo.findDelta(namespace, environment, sinceChangeSeq);
    final List<ConfigEntryResponse> items = delta.stream().map(this::toResponse).toList();
    final long snapshotChangeSeq =
        items.stream().mapToLong(ConfigEntryResponse::getChangeSeq).max().orElse(sinceChangeSeq);

    return DeltaPollResponse.builder()
        .namespace(namespace)
        .environment(environment)
        .snapshotChangeSeq(snapshotChangeSeq)
        .pollIntervalSeconds(props.getPollIntervalSeconds())
        .items(items)
        .build();
  }

  public ConfigEntryResponse getSingleKey(
      final String namespace, final String environment, final String key) {
    return entryRepo
        .findByNamespaceAndEnvironmentAndKey(namespace, environment, key)
        .map(this::toResponse)
        .orElseThrow(() -> new ConfigEntryNotFoundException(namespace, environment, key));
  }

  public PagedConfigResponse search(
      final String namespace,
      final String environment,
      final String searchTerm,
      final int page,
      final int size) {
    final Page<ConfigEntry> result =
        entryRepo.search(namespace, environment, searchTerm, PageRequest.of(page, size));
    return PagedConfigResponse.builder()
        .items(result.getContent().stream().map(this::toResponse).toList())
        .total(result.getTotalElements())
        .page(page)
        .totalPages(result.getTotalPages())
        .build();
  }

  @Transactional
  public ConfigEntryResponse create(
      final CreateConfigRequest request, final String callerIdentity) {
    if (entryRepo.existsByNamespaceAndEnvironmentAndKey(
        request.getNamespace(), request.getEnvironment(), request.getKey())) {
      throw new DuplicateConfigEntryException(
          request.getNamespace(), request.getEnvironment(), request.getKey());
    }

    final ConfigEntry entry =
        ConfigEntry.builder()
            .namespace(request.getNamespace())
            .environment(request.getEnvironment())
            .key(request.getKey())
            .valueType(request.getValueType())
            .value(request.getValue())
            .description(request.getDescription())
            .owner(request.getOwner())
            .updatedBy(callerIdentity)
            .build();

    final ConfigEntry saved = entryRepo.save(entry);
    // flush sends the INSERT, then refresh reloads the trigger-set version/created_at
    entityManager.flush();
    entityManager.refresh(saved);
    return toResponse(saved);
  }

  @Transactional
  public ConfigEntryResponse update(
      final UUID id, final UpdateConfigRequest request, final String callerIdentity) {
    final ConfigEntry entry =
        entryRepo.findById(id).orElseThrow(() -> new ConfigEntryNotFoundException(id));

    if (!entry.getChangeSeq().equals(request.getExpectedChangeSeq())) {
      throw new ConfigEntryConflictException(request.getExpectedChangeSeq(), entry.getChangeSeq());
    }

    entry.setValue(request.getValue());
    entry.setUpdatedBy(callerIdentity);
    if (request.getDescription() != null) {
      entry.setDescription(request.getDescription());
    }
    if (request.getOwner() != null) {
      entry.setOwner(request.getOwner());
    }

    final ConfigEntry merged = entryRepo.save(entry);
    // flush sends the UPDATE, then refresh picks up the trigger-bumped version/updated_at
    entityManager.flush();
    entityManager.refresh(merged);
    return toResponse(merged);
  }

  @Transactional
  public void softDelete(final UUID id, final String callerIdentity) {
    final ConfigEntry entry =
        entryRepo.findById(id).orElseThrow(() -> new ConfigEntryNotFoundException(id));
    entry.setStatus(EntryStatusEnum.DELETED);
    entry.setUpdatedBy(callerIdentity);
    entryRepo.save(entry);
  }

  public List<HistoryEntryResponse> getHistory(final UUID id) {
    if (!entryRepo.existsById(id)) {
      throw new ConfigEntryNotFoundException(id);
    }
    return historyRepo.findByEntryIdOrderByChangedAtDesc(id).stream()
        .map(this::toHistoryResponse)
        .toList();
  }

  private ConfigEntryResponse toResponse(final ConfigEntry e) {
    return ConfigEntryResponse.builder()
        .id(e.getId())
        .namespace(e.getNamespace())
        .environment(e.getEnvironment())
        .key(e.getKey())
        .valueType(e.getValueType())
        .value(e.getValue())
        .status(e.getStatus())
        .description(e.getDescription())
        .owner(e.getOwner())
        .changeSeq(e.getChangeSeq())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .updatedBy(e.getUpdatedBy())
        .build();
  }

  private HistoryEntryResponse toHistoryResponse(final ConfigEntryHistory h) {
    return HistoryEntryResponse.builder()
        .historyId(h.getHistoryId())
        .key(h.getKey())
        .oldValue(h.getOldValue())
        .newValue(h.getNewValue())
        .oldStatus(h.getOldStatus())
        .newStatus(h.getNewStatus())
        .changeSeq(h.getChangeSeq())
        .changedAt(h.getChangedAt())
        .changedBy(h.getChangedBy())
        .changeReason(h.getChangeReason())
        .build();
  }
}
