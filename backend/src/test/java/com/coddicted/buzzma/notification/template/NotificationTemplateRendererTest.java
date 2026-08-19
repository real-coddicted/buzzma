package com.coddicted.buzzma.notification.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.config.ConfigProvider;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateRendererTest {

  private static final String KEY = "claim-decision-notification.approved.title";
  private static final String DEFAULT_TEMPLATE = "Hurray! Claim {claimCode} approved!!";

  @Mock private ConfigProvider mockConfigProvider;

  @Test
  void testRenderFallsBackToDefaultTemplateWhenConfigKeyUnset() {
    when(mockConfigProvider.getString(KEY, DEFAULT_TEMPLATE)).thenReturn(DEFAULT_TEMPLATE);
    final NotificationTemplateRenderer renderer =
        new NotificationTemplateRenderer(mockConfigProvider);

    final String result = renderer.render(KEY, DEFAULT_TEMPLATE, Map.of("claimCode", "CLM-42"));

    assertEquals("Hurray! Claim CLM-42 approved!!", result);
  }

  @Test
  void testRenderUsesConfigSuppliedTemplateOverDefault() {
    when(mockConfigProvider.getString(KEY, DEFAULT_TEMPLATE))
        .thenReturn("Your claim {claimCode} is now approved.");
    final NotificationTemplateRenderer renderer =
        new NotificationTemplateRenderer(mockConfigProvider);

    final String result = renderer.render(KEY, DEFAULT_TEMPLATE, Map.of("claimCode", "CLM-42"));

    assertEquals("Your claim CLM-42 is now approved.", result);
  }

  @Test
  void testRenderSubstitutesMultiplePlaceholders() {
    final String template = "Claim {claimCode} rejected: {reviewerComment}";
    when(mockConfigProvider.getString("k", template)).thenReturn(template);
    final NotificationTemplateRenderer renderer =
        new NotificationTemplateRenderer(mockConfigProvider);

    final String result =
        renderer.render(
            "k", template, Map.of("claimCode", "CLM-7", "reviewerComment", "blurry screenshot"));

    assertEquals("Claim CLM-7 rejected: blurry screenshot", result);
  }

  @Test
  void testRenderLeavesUnmatchedPlaceholdersUntouched() {
    final String template = "Claim {claimCode} status: {status}";
    when(mockConfigProvider.getString("k", template)).thenReturn(template);
    final NotificationTemplateRenderer renderer =
        new NotificationTemplateRenderer(mockConfigProvider);

    final String result = renderer.render("k", template, Map.of("claimCode", "CLM-9"));

    assertEquals("Claim CLM-9 status: {status}", result);
  }
}
