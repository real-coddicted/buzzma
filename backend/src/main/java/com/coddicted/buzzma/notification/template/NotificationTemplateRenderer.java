package com.coddicted.buzzma.notification.template;

import com.coddicted.buzzma.config.ConfigProvider;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Renders notification title/message text from a config-sdk-backed template, substituting {@code
 * {placeholderName}} tokens with caller-supplied values. Falls back to the caller-provided default
 * template when config-sdk is inert or the key is unset (see {@link ConfigProvider}).
 */
@Component
public class NotificationTemplateRenderer {

  private final ConfigProvider configProvider;

  public NotificationTemplateRenderer(final ConfigProvider configProvider) {
    this.configProvider = configProvider;
  }

  public String render(
      final String configKey,
      final String defaultTemplate,
      final Map<String, String> placeholders) {
    String rendered = configProvider.getString(configKey, defaultTemplate);
    for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
      rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
    }
    return rendered;
  }
}
