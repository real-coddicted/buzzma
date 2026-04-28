package com.coddicted.buzzma.mediator.service.impl;

import com.coddicted.buzzma.agency.api.AgencyQueryPort;
import com.coddicted.buzzma.mediator.persistence.MediatorProfilesRepository;
import com.coddicted.buzzma.mediator.service.LineageService;
import com.coddicted.buzzma.shared.enums.AgencyStatus;
import com.coddicted.buzzma.shared.enums.MediatorStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LineageServiceImpl implements LineageService {

  private static final long TTL_MS = 60_000L;
  private static final int MAX_ENTRIES = 2000;

  private final AgencyQueryPort agencyQueryPort;
  private final MediatorProfilesRepository mediatorProfilesRepository;

  // Simple TTL cache: key -> CacheEntry(value, expiry)
  private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

  public LineageServiceImpl(
      AgencyQueryPort agencyQueryPort, MediatorProfilesRepository mediatorProfilesRepository) {
    this.agencyQueryPort = agencyQueryPort;
    this.mediatorProfilesRepository = mediatorProfilesRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> listMediatorCodesForAgency(String agencyCode) {
    String key = "mediators_for_" + agencyCode;
    @SuppressWarnings("unchecked")
    List<String> cached = (List<String>) getFromCache(key);
    if (cached != null) {
      return cached;
    }
    List<String> codes =
        mediatorProfilesRepository.findAllByParentAgencyCodeAndIsDeletedFalse(agencyCode).stream()
            .map(m -> m.getMediatorCode())
            .toList();
    putInCache(key, codes);
    return codes;
  }

  @Override
  @Transactional(readOnly = true)
  public String getAgencyCodeForMediatorCode(String mediatorCode) {
    String key = "agency_for_" + mediatorCode;
    String cached = (String) getFromCache(key);
    if (cached != null) {
      return cached;
    }
    String agencyCode =
        mediatorProfilesRepository
            .findByMediatorCode(mediatorCode)
            .map(m -> m.getParentAgencyCode())
            .orElse(null);
    if (agencyCode != null) {
      putInCache(key, agencyCode);
    }
    return agencyCode;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isAgencyActive(String agencyCode) {
    String key = "agency_active_" + agencyCode;
    Boolean cached = (Boolean) getFromCache(key);
    if (cached != null) {
      return cached;
    }
    boolean active =
        agencyQueryPort
            .findStatusByCode(agencyCode)
            .map(s -> AgencyStatus.active.equals(s))
            .orElse(false);
    putInCache(key, active);
    return active;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isMediatorActive(String mediatorCode) {
    String key = "mediator_active_" + mediatorCode;
    Boolean cached = (Boolean) getFromCache(key);
    if (cached != null) {
      return cached;
    }
    boolean active =
        mediatorProfilesRepository
            .findByMediatorCode(mediatorCode)
            .map(m -> MediatorStatus.active.equals(m.getStatus()))
            .orElse(false);
    putInCache(key, active);
    return active;
  }

  @Override
  public void clearCache() {
    cache.clear();
  }

  @Scheduled(fixedRate = 300_000L)
  public void evictExpiredEntries() {
    Instant now = Instant.now();
    cache.entrySet().removeIf(e -> e.getValue().expiry.isBefore(now));
    // LRU eviction: if still over limit, remove oldest entries
    if (cache.size() > MAX_ENTRIES) {
      List<Map.Entry<String, CacheEntry>> entries = new ArrayList<>(cache.entrySet());
      entries.sort((a, b) -> a.getValue().expiry.compareTo(b.getValue().expiry));
      int toRemove = cache.size() - MAX_ENTRIES;
      for (int i = 0; i < toRemove; i++) {
        cache.remove(entries.get(i).getKey());
      }
    }
  }

  private Object getFromCache(String key) {
    CacheEntry entry = cache.get(key);
    if (entry == null || entry.expiry.isBefore(Instant.now())) {
      cache.remove(key);
      return null;
    }
    return entry.value;
  }

  private void putInCache(String key, Object value) {
    cache.put(key, new CacheEntry(value, Instant.now().plusMillis(TTL_MS)));
  }

  private record CacheEntry(Object value, Instant expiry) {}
}
