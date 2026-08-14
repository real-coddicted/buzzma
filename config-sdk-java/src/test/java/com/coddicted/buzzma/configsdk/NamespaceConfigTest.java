package com.coddicted.buzzma.configsdk;

import static org.assertj.core.api.Assertions.assertThat;

import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.coddicted.buzzma.configsdk.model.ValueType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NamespaceConfigTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private NamespaceState state;
  private NamespaceConfig config;

  @BeforeEach
  void setUp() {
    state = new NamespaceState("checkout-service", "prod", 60);
    config = new NamespaceConfig(state, objectMapper);
  }

  @Test
  void getBoolReturnsCachedValueForMatchingType() {
    seed("new_checkout_flow", ValueType.BOOLEAN, BooleanNode.TRUE);
    assertThat(config.getBool("new_checkout_flow", false)).isTrue();
  }

  @Test
  void getBoolFallsBackToDefaultWhenKeyMissing() {
    assertThat(config.getBool("unknown_key", true)).isTrue();
  }

  @Test
  void getBoolFallsBackToDefaultOnTypeMismatch() {
    seed("checkout_timeout_ms", ValueType.NUMBER, new DoubleNode(500));
    assertThat(config.getBool("checkout_timeout_ms", false)).isFalse();
  }

  @Test
  void getStringReturnsCachedValue() {
    seed("banner_text", ValueType.STRING, new TextNode("Sale ends soon"));
    assertThat(config.getString("banner_text", "default")).isEqualTo("Sale ends soon");
  }

  @Test
  void getIntAndGetDoubleReadNumberValues() {
    seed("max_retries", ValueType.NUMBER, new DoubleNode(3));
    assertThat(config.getInt("max_retries", 0)).isEqualTo(3);
    assertThat(config.getDouble("max_retries", 0.0)).isEqualTo(3.0);
  }

  @Test
  void getJsonConvertsToRequestedType() {
    seed("limits", ValueType.JSON, objectMapper.valueToTree(Map.of("max", 10)));
    final Limits limits = config.getJson("limits", Limits.class, new Limits(0));
    assertThat(limits.max()).isEqualTo(10);
  }

  @Test
  void getJsonFallsBackToDefaultOnTypeMismatch() {
    seed("limits", ValueType.STRING, new TextNode("not-json-shaped"));
    final Limits fallback = new Limits(-1);
    assertThat(config.getJson("limits", Limits.class, fallback)).isEqualTo(fallback);
  }

  @Test
  void getJsonFallsBackToDefaultWhenShapeDoesNotConvert() {
    // valueType is JSON but the payload shape (an array) can't convert to a Limits record.
    seed("limits", ValueType.JSON, objectMapper.valueToTree(java.util.List.of(1, 2, 3)));
    final Limits fallback = new Limits(-1);
    assertThat(config.getJson("limits", Limits.class, fallback)).isEqualTo(fallback);
  }

  @Test
  void cacheHitAndMissCountsAreTracked() {
    seed("flag", ValueType.BOOLEAN, BooleanNode.TRUE);
    config.getBool("flag", false);
    config.getBool("missing", false);
    assertThat(state.cacheHitCount()).isEqualTo(1);
    assertThat(state.cacheMissCount()).isEqualTo(1);
  }

  private void seed(final String key, final ValueType type, final JsonNode value) {
    final ConfigEntry entry =
        ConfigEntry.builder().key(key).valueType(type).value(value).changeSeq(1).build();
    state.replaceCache(Map.of(key, entry), 1);
  }

  record Limits(int max) {}
}
