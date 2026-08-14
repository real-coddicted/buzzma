package com.coddicted.buzzma.configsdk.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.coddicted.buzzma.configsdk.model.ValueType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DiskSnapshotStoreTest {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @TempDir Path tempDir;

  @Test
  void writeThenReadRoundTripsEntries() {
    final DiskSnapshotStore store = new DiskSnapshotStore(tempDir, objectMapper);
    final ConfigEntry entry =
        ConfigEntry.builder()
            .key("new_checkout_flow")
            .valueType(ValueType.BOOLEAN)
            .value(BooleanNode.TRUE)
            .changeSeq(7)
            .build();

    store.write("checkout-service", "prod", 7, List.of(entry));
    final Optional<SnapshotFile> loaded = store.read("checkout-service", "prod");

    assertThat(loaded).isPresent();
    assertThat(loaded.get().getSnapshotChangeSeq()).isEqualTo(7);
    assertThat(loaded.get().getFormatVersion()).isEqualTo(SnapshotFile.CURRENT_FORMAT_VERSION);
    assertThat(loaded.get().getItems()).hasSize(1);
    assertThat(loaded.get().getItems().get(0).getKey()).isEqualTo("new_checkout_flow");
  }

  @Test
  void readReturnsEmptyWhenNoSnapshotExists() {
    final DiskSnapshotStore store = new DiskSnapshotStore(tempDir, objectMapper);
    assertThat(store.read("never-written", "prod")).isEmpty();
  }

  @Test
  void readIgnoresSnapshotWithUnsupportedFormatVersion() throws Exception {
    final DiskSnapshotStore store = new DiskSnapshotStore(tempDir, objectMapper);
    final SnapshotFile futureFormat =
        SnapshotFile.builder()
            .formatVersion(SnapshotFile.CURRENT_FORMAT_VERSION + 1)
            .namespace("checkout-service")
            .environment("prod")
            .snapshotChangeSeq(1)
            .writtenAt(Instant.now())
            .items(List.of())
            .build();
    Files.createDirectories(tempDir);
    Files.writeString(
        tempDir.resolve("checkout-service__prod.json"),
        objectMapper.writeValueAsString(futureFormat));

    assertThat(store.read("checkout-service", "prod")).isEmpty();
  }

  @Test
  void writeSanitizesUnsafeCharactersInFileName() {
    final DiskSnapshotStore store = new DiskSnapshotStore(tempDir, objectMapper);
    store.write("team/checkout service", "prod", 1, List.of());
    assertThat(store.read("team/checkout service", "prod")).isPresent();
  }
}
