package com.coddicted.buzzma.configsdk.snapshot;

import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One JSON file per {@code namespace+environment}, written after every successful fetch/poll, read
 * only at startup (design doc §7). Purpose is narrow: survive a process restart on the same host
 * when the Config API happens to be unreachable at that exact moment — not a general persistence
 * layer.
 */
public class DiskSnapshotStore {

  private static final Logger LOG = LoggerFactory.getLogger(DiskSnapshotStore.class);

  private final Path snapshotDir;
  private final ObjectMapper objectMapper;

  public DiskSnapshotStore(final Path snapshotDir, final ObjectMapper objectMapper) {
    this.snapshotDir = snapshotDir;
    this.objectMapper = objectMapper;
  }

  public void write(
      final String namespace,
      final String environment,
      final long snapshotChangeSeq,
      final Collection<ConfigEntry> entries) {
    final SnapshotFile snapshot =
        SnapshotFile.builder()
            .formatVersion(SnapshotFile.CURRENT_FORMAT_VERSION)
            .namespace(namespace)
            .environment(environment)
            .snapshotChangeSeq(snapshotChangeSeq)
            .writtenAt(Instant.now())
            .items(List.copyOf(entries))
            .build();

    final Path target = fileFor(namespace, environment);
    final Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
    try {
      Files.createDirectories(snapshotDir);
      Files.writeString(tmp, objectMapper.writeValueAsString(snapshot));
      // Atomic rename so a crash mid-write never leaves a corrupt/partial snapshot on disk.
      Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (final IOException e) {
      LOG.warn(
          "Failed to write config snapshot for {}/{}: {}", namespace, environment, e.getMessage());
    }
  }

  public Optional<SnapshotFile> read(final String namespace, final String environment) {
    final Path file = fileFor(namespace, environment);
    if (!Files.isRegularFile(file)) {
      return Optional.empty();
    }
    try {
      final SnapshotFile snapshot = objectMapper.readValue(file.toFile(), SnapshotFile.class);
      if (snapshot.getFormatVersion() != SnapshotFile.CURRENT_FORMAT_VERSION) {
        LOG.warn(
            "Ignoring config snapshot for {}/{}: format_version {} unsupported (expected {})",
            namespace,
            environment,
            snapshot.getFormatVersion(),
            SnapshotFile.CURRENT_FORMAT_VERSION);
        return Optional.empty();
      }
      return Optional.of(snapshot);
    } catch (final IOException e) {
      LOG.warn(
          "Failed to read config snapshot for {}/{}: {}", namespace, environment, e.getMessage());
      return Optional.empty();
    }
  }

  private Path fileFor(final String namespace, final String environment) {
    final String safeNamespace = sanitize(namespace);
    final String safeEnvironment = sanitize(environment);
    return snapshotDir.resolve(safeNamespace + "__" + safeEnvironment + ".json");
  }

  private String sanitize(final String value) {
    return value.replaceAll("[^a-zA-Z0-9_-]", "_");
  }
}
