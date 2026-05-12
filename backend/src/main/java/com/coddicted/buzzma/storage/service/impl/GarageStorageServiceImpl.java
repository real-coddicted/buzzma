package com.coddicted.buzzma.storage.service.impl;

import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "garage")
public class GarageStorageServiceImpl implements StorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GarageStorageServiceImpl.class);

  private final S3Client s3Client;
  private final String bucket;

  public GarageStorageServiceImpl(
      final S3Client s3Client, @Qualifier("bucketName") final String bucket) {
    this.bucket = bucket;
    this.s3Client = s3Client;
  }

  @Override
  public String store(
      final String folder,
      final String originalFilename,
      final String contentType,
      final byte[] data) {
    final String ext = extractExtension(originalFilename);
    final String storageKey = folder + "/" + UUID.randomUUID() + ext;

    final PutObjectRequest putRequest =
        PutObjectRequest.builder()
            .bucket(this.bucket)
            .key(storageKey)
            .contentType(contentType)
            .build();

    this.s3Client.putObject(putRequest, RequestBody.fromBytes(data));
    LOGGER.debug("Stored file in Garage: bucket={}, key={}", this.bucket, storageKey);
    return storageKey;
  }

  @Override
  public byte[] retrieve(final String storageKey) {
    try {
      final GetObjectRequest getRequest =
          GetObjectRequest.builder().bucket(this.bucket).key(storageKey).build();

      final ResponseBytes<?> responseBytes =
          this.s3Client.getObject(getRequest, ResponseTransformer.toBytes());
      return responseBytes.asByteArray();
    } catch (final NoSuchKeyException e) {
      throw new NotFoundException("File not found: " + storageKey);
    }
  }

  @Override
  public void delete(final String storageKey) {
    try {
      final DeleteObjectRequest deleteRequest =
          DeleteObjectRequest.builder().bucket(this.bucket).key(storageKey).build();

      this.s3Client.deleteObject(deleteRequest);
      LOGGER.debug("Deleted file from Garage: bucket={}, key={}", this.bucket, storageKey);
    } catch (final Exception e) {
      LOGGER.warn("Failed to delete file {}: {}", storageKey, e.getMessage());
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
