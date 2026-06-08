package com.coddicted.buzzma.storage.service.impl;

import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.config.StorageProperties;
import com.coddicted.buzzma.storage.service.StorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local")
public class LocalStorageServiceImpl implements StorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

  private final StorageProperties properties;

  public LocalStorageServiceImpl(final StorageProperties properties) {
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
      final Path target = Paths.get(this.properties.getBaseDir()).resolve(storageKey);
      Files.createDirectories(target.getParent());
      Files.write(target, data);
      LOGGER.debug("Stored file at {}", storageKey);
      return storageKey;
    } catch (final IOException e) {
      throw new RuntimeException("Failed to store file: " + originalFilename, e);
    }
  }

  @Override
  public ResponseBytes<GetObjectResponse> retrieve(final String storageKey) {
    try {
      final Path path = Paths.get(this.properties.getBaseDir()).resolve(storageKey);
      if (!Files.exists(path)) {
        throw new NotFoundException("File not found: " + storageKey);
      }
      final byte[] data = Files.readAllBytes(path);
      final String contentType = Files.probeContentType(path);
      final GetObjectResponse sdkResponse =
          GetObjectResponse.builder().contentType(contentType).build();
      return ResponseBytes.fromByteArray(sdkResponse, data);
    } catch (final IOException e) {
      throw new RuntimeException("Failed to retrieve file: " + storageKey, e);
    }
  }

  @Override
  public void delete(final String storageKey) {
    try {
      final Path path = Paths.get(this.properties.getBaseDir()).resolve(storageKey);
      Files.deleteIfExists(path);
      LOGGER.debug("Deleted file at {}", storageKey);
    } catch (final IOException e) {
      LOGGER.warn("Failed to delete file at {}: {}", storageKey, e.getMessage());
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
