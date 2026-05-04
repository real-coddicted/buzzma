package com.coddicted.buzzma.shared.storage;

import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LocalStorageService implements StorageService {

  private static final Logger LOG = LoggerFactory.getLogger(LocalStorageService.class);

  private final StorageProperties properties;

  public LocalStorageService(final StorageProperties properties) {
    this.properties = properties;
  }

  @Override
  public String store(
      final String folder,
      final String originalFilename,
      final String contentType,
      final byte[] data) {
    try {
      final String ext = extractExtension(originalFilename);
      final String storageKey = folder + "/" + UUID.randomUUID() + ext;
      final Path target = Paths.get(properties.getBaseDir()).resolve(storageKey);
      Files.createDirectories(target.getParent());
      Files.write(target, data);
      LOG.debug("Stored file at {}", storageKey);
      return storageKey;
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file: " + originalFilename, e);
    }
  }

  @Override
  public byte[] retrieve(final String storageKey) {
    try {
      final Path path = Paths.get(properties.getBaseDir()).resolve(storageKey);
      if (!Files.exists(path)) {
        throw new NotFoundException("File not found: " + storageKey);
      }
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve file: " + storageKey, e);
    }
  }

  @Override
  public void delete(final String storageKey) {
    try {
      final Path path = Paths.get(properties.getBaseDir()).resolve(storageKey);
      Files.deleteIfExists(path);
      LOG.debug("Deleted file at {}", storageKey);
    } catch (IOException e) {
      LOG.warn("Failed to delete file at {}: {}", storageKey, e.getMessage());
    }
  }

  private String extractExtension(final String filename) {
    if (filename == null) {
      return "";
    }
    final int dot = filename.lastIndexOf('.');
    return dot >= 0 ? filename.substring(dot) : "";
  }
}
