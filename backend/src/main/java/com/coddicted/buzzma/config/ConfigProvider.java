package com.coddicted.buzzma.config;

import com.coddicted.buzzma.configsdk.ConfigClient;
import com.coddicted.buzzma.configsdk.NamespaceConfig;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Single point of access to the {@code backend} namespace's runtime config/feature-flag values.
 * Falls back to the caller-provided default when config-sdk.api-url is unset (SDK bean absent).
 */
@Component
public class ConfigProvider {

  private static final String NAMESPACE = "backend";

  private final NamespaceConfig namespaceConfig;

  public ConfigProvider(final Optional<ConfigClient> configClient) {
    this.namespaceConfig = configClient.map(c -> c.forNamespace(NAMESPACE)).orElse(null);
  }

  public boolean getBool(final String key, final boolean defaultValue) {
    return namespaceConfig == null ? defaultValue : namespaceConfig.getBool(key, defaultValue);
  }

  public String getString(final String key, final String defaultValue) {
    return namespaceConfig == null ? defaultValue : namespaceConfig.getString(key, defaultValue);
  }

  public int getInt(final String key, final int defaultValue) {
    return namespaceConfig == null ? defaultValue : namespaceConfig.getInt(key, defaultValue);
  }

  public double getDouble(final String key, final double defaultValue) {
    return namespaceConfig == null ? defaultValue : namespaceConfig.getDouble(key, defaultValue);
  }

  public <T> T getJson(final String key, final Class<T> type, final T defaultValue) {
    return namespaceConfig == null
        ? defaultValue
        : namespaceConfig.getJson(key, type, defaultValue);
  }
}
