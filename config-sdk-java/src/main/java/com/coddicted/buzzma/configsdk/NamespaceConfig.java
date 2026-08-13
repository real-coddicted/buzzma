package com.coddicted.buzzma.configsdk;

import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.coddicted.buzzma.configsdk.model.ValueType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronous, lock-free read API for one namespace's cached config values. Every getter takes a
 * caller-provided local default — the last link in the fallback chain, since only the calling code
 * knows what "safe" means for that specific flag if every other layer has failed (design doc §7).
 * Reads never make a network call; they read the poller's last-swapped in-memory snapshot.
 */
public class NamespaceConfig {

  private static final Logger LOG = LoggerFactory.getLogger(NamespaceConfig.class);

  private final NamespaceState state;
  private final ObjectMapper objectMapper;

  NamespaceConfig(final NamespaceState state, final ObjectMapper objectMapper) {
    this.state = state;
    this.objectMapper = objectMapper;
  }

  public boolean getBool(final String key, final boolean defaultValue) {
    final JsonNode value = valueOfType(key, ValueType.BOOLEAN);
    return value == null ? defaultValue : value.asBoolean(defaultValue);
  }

  public String getString(final String key, final String defaultValue) {
    final JsonNode value = valueOfType(key, ValueType.STRING);
    return value == null ? defaultValue : value.asText(defaultValue);
  }

  public int getInt(final String key, final int defaultValue) {
    final JsonNode value = valueOfType(key, ValueType.NUMBER);
    return value == null || !value.isNumber() ? defaultValue : value.asInt(defaultValue);
  }

  public double getDouble(final String key, final double defaultValue) {
    final JsonNode value = valueOfType(key, ValueType.NUMBER);
    return value == null || !value.isNumber() ? defaultValue : value.asDouble(defaultValue);
  }

  public <T> T getJson(final String key, final Class<T> type, final T defaultValue) {
    final JsonNode value = valueOfType(key, ValueType.JSON);
    if (value == null) {
      return defaultValue;
    }
    try {
      return objectMapper.treeToValue(value, type);
    } catch (final Exception e) {
      LOG.warn("Config key '{}' value could not be converted to {}: {}", key, type, e.getMessage());
      return defaultValue;
    }
  }

  private JsonNode valueOfType(final String key, final ValueType expectedType) {
    final ConfigEntry entry = state.cacheSnapshot().get(key);
    if (entry == null) {
      state.recordCacheMiss();
      return null;
    }
    state.recordCacheHit();
    if (entry.getValueType() != expectedType) {
      LOG.warn(
          "Config key '{}' is of type {} but was requested as {}; using caller default",
          key,
          entry.getValueType(),
          expectedType);
      return null;
    }
    return entry.getValue();
  }
}
